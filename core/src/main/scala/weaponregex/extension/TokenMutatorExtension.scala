package weaponregex.extension

import weaponregex.model.mutation.TokenMutator

object TokenMutatorExtension {

  /** The extension that filter a given sequence of [[weaponregex.model.mutation.TokenMutator]]
    */
  implicit class TokenMutatorsFiltering(mutators: Seq[TokenMutator]) {

    /** Filter token mutators based on the given mutation level
      * @param mutationLevel Target mutation level
      * @return Sequence of token mutators in the given mutation levels
      */
    def atLevel(mutationLevel: Int): Seq[TokenMutator] =
      mutators filter (_.levels.contains(mutationLevel))

    /** Filter token mutators based on the given mutation levels
      * @param mutationLevels Target mutation levels
      * @return Sequence of token mutators in the given mutation levels
      */
    def atLevels(mutationLevels: Seq[Int]): Seq[TokenMutator] =
      mutators filter (mutator => mutationLevels exists (mutator.levels contains _))
  }
}
