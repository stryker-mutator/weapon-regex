package weaponregex.internal.extension

import cats.data.{NonEmptyList, NonEmptySet}
import cats.syntax.all.*
import weaponregex.model.mutation.TokenMutator

private[weaponregex] object TokenMutatorExtension {

  /** The extension that filter a given sequence of [[weaponregex.model.mutation.TokenMutator]]
    */
  implicit class TokenMutatorsFiltering(val mutators: NonEmptyList[TokenMutator]) extends AnyVal {

    /** Filter token mutators based on the given mutation level
      * @param mutationLevel
      *   Target mutation level
      * @return
      *   Sequence of token mutators in the given mutation levels
      */
    def atLevel(mutationLevel: Int): Option[NonEmptyList[TokenMutator]] =
      mutators.filter(_.levels.contains(mutationLevel)).toNel

    /** Filter token mutators based on the given mutation levels
      * @param mutationLevels
      *   Target mutation levels
      * @return
      *   Sequence of token mutators in the given mutation levels
      */
    def atLevels(mutationLevels: NonEmptySet[Int]): Option[NonEmptyList[TokenMutator]] =
      mutators.filter(mutator => mutationLevels.exists(mutator.levels.contains(_))).toNel
  }
}
