package weaponregex.extension

import weaponregex.extension.TokenMutatorExtension.TokenMutatorsFiltering
import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree._
import weaponregex.mutator.BuiltinMutators

object RegexTreeExtension {

  /** The extension that build a given [[weaponregex.model.regextree.RegexTree]] into a string
    */
  implicit class RegexTreeStringBuilder(tree: RegexTree) {

    /** Build the tree into a String
      */
    lazy val build: String = tree match {
      case leaf: Leaf[_]  => leaf.prefix + leaf.value + leaf.postfix
      case ft: FlagToggle => ft.onFlags.build + (if (ft.hasDash) "-" else "") + ft.offFlags.build
      case _              => buildWhile(_ => true)
    }

    /** Build the tree into a String with a child replaced by a string.
      * @param child
      *   Child to be replaced
      * @param childString
      *   Replacement String
      * @return
      *   A String representation of the tree
      */
    def buildWith(child: RegexTree, childString: String): String = tree match {
      case _: Leaf[_] => build
      case _ =>
        tree.children
          .map(c => if (c eq child) childString else c.build)
          .mkString(tree.prefix, tree.sep, tree.postfix)
    }

    /** Build the tree into a String while a predicate holds for a given child.
      * @param pred
      *   Predicate on a child
      * @return
      *   A String representation of the tree
      */
    def buildWhile(pred: RegexTree => Boolean): String = tree match {
      case _: Leaf[_] => build
      case _ =>
        tree.children
          .filter(pred)
          .map(_.build)
          .mkString(tree.prefix, tree.sep, tree.postfix)
    }
  }

  /** The extension that traverses and mutates a given [[weaponregex.model.regextree.RegexTree]]
    */
  implicit class RegexTreeMutator(tree: RegexTree) {

    /** Mutate using the given mutators in some specific mutation levels
      *
      * @param mutators
      *   Mutators to be used for mutation
      * @param mutationLevels
      *   Target mutation levels. If this is `null`, the `mutators` will not be filtered.
      * @return
      *   A sequence of [[weaponregex.model.mutation.Mutant]]
      */
    def mutate(mutators: Seq[TokenMutator] = BuiltinMutators.all, mutationLevels: Seq[Int] = null): Seq[Mutant] = {
      if (mutationLevels != null) return mutate(mutators.atLevels(mutationLevels))

      val rootMutants: Seq[Mutant] = mutators flatMap (_(tree))
      val childrenMutants: Seq[Mutant] = tree.children flatMap (child =>
        child.mutate(mutators) map (mutant => mutant.copy(pattern = tree.buildWith(child, mutant.pattern)))
      )
      rootMutants ++ childrenMutants
    }
  }
}
