package weaponregex.mutator

import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree.RegexTree

/** The object that traverses and mutates a given [[weaponregex.model.regextree.RegexTree]]
  */
object TreeMutator {
  implicit class RegexTreeMutator(tree: RegexTree) {

    /** Filter token mutators based on the given mutation levels
      * @param mutators Token mutators to be filtered
      * @param mutationLevels Target mutation levels
      * @return Sequence of token mutators in the given mutation levels
      */
    private def filterMutators(mutators: Seq[TokenMutator], mutationLevels: Seq[Int]): Seq[TokenMutator] =
      mutators.filter(mutator => mutationLevels.exists(mutator.levels.contains(_)))

    /** Mutate using the given mutators in some specific mutation levels
      *
      * @param mutators Mutators to be used for mutation
      * @param mutationLevels Target mutation levels. If this is `null`, the `mutators` will not be filtered.
      * @return A sequence of [[weaponregex.model.mutation.Mutant]]
      */
    def mutate(mutators: Seq[TokenMutator] = BuiltinMutators.all, mutationLevels: Seq[Int] = null): Seq[Mutant] = {
      val mutatorsFiltered: Seq[TokenMutator] =
        if (mutationLevels == null) mutators
        else filterMutators(mutators, mutationLevels)

      val rootMutants: Seq[Mutant] = mutatorsFiltered flatMap (mutator =>
        mutator(tree) map (mutatedPattern =>
          Mutant(mutatedPattern, mutator.name, tree.location, mutator.levels, mutator.description)
        )
      )

      val childrenMutants: Seq[Mutant] = tree.children flatMap (child =>
        child.mutate(mutatorsFiltered) map { case mutant @ Mutant(mutatedPattern, _, _, _, _) =>
          mutant.copy(pattern = tree.buildWith(child, mutatedPattern))
        }
      )

      rootMutants ++ childrenMutants
    }
  }
}
