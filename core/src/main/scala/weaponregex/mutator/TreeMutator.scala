package weaponregex.mutator

import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree.RegexTree
import weaponregex.`extension`.TokenMutatorExtension.TokenMutatorsFiltering

/** The object that traverses and mutates a given [[weaponregex.model.regextree.RegexTree]]
  */
object TreeMutator {
  implicit class RegexTreeMutator(tree: RegexTree) {

    /** Mutate using the given mutators in some specific mutation levels
      *
      * @param mutators Mutators to be used for mutation
      * @param mutationLevels Target mutation levels. If this is `null`, the `mutators` will not be filtered.
      * @return A sequence of [[weaponregex.model.mutation.Mutant]]
      */
    def mutate(mutators: Seq[TokenMutator] = BuiltinMutators.all, mutationLevels: Seq[Int] = null): Seq[Mutant] =
      mutate(
        if (mutationLevels == null) mutators
        else mutators.atLevels(mutationLevels)
      )

    /** Mutate using the given mutators
      *
      * @param mutators Mutators to be used for mutation
      * @return A sequence of [[weaponregex.model.mutation.Mutant]]
      */
    private def mutate(mutators: Seq[TokenMutator]): Seq[Mutant] = {
      val rootMutants: Seq[Mutant] = mutators flatMap (_(tree))
      val childrenMutants: Seq[Mutant] = tree.children flatMap (child =>
        child.mutate(mutators) map (mutant => mutant.copy(pattern = tree.buildWith(child, mutant.pattern)))
      )
      rootMutants ++ childrenMutants
    }
  }
}
