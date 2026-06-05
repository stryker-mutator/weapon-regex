package weaponregex.model.mutation

import cats.data.NonEmptySet
import mutationtesting.Location
import weaponregex.internal.model.regextree.RegexTree

trait TokenMutator {

  /** The name of the mutator
    */
  val name: String

  /** The mutation levels that the token mutator falls under
    */
  val levels: NonEmptySet[Int]

  /** Generate the default description for the mutants of this mutator
    * @param original
    *   The original token string being mutated
    * @param mutated
    *   The mutated string
    * @param location
    *   The [[mutationtesting.Location]] where the mutation occurred
    */
  def description(original: String, mutated: String, location: Location): String =
    s"${location.show} Mutate $original to $mutated"

  /** Mutate the given token
    * @param token
    *   Target token
    * @return
    *   Sequence of [[weaponregex.model.mutation.Mutant]]
    */
  def mutate(token: RegexTree): Seq[Mutant]

}
