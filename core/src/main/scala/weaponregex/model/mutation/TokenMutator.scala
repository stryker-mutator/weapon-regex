package weaponregex.model.mutation

import weaponregex.model.Location
import weaponregex.model.regextree.RegexTree

trait TokenMutator {

  /** The name of the mutator
    */
  def name: String

  /** The mutation levels that the token mutator falls under
    */
  def levels: Seq[Int]

  /** A short description of the mutator
    */
  def description: String

  /** Apply mutation to the given token
    * @param token
    *   Target token
    * @return
    *   Sequence of [[weaponregex.model.mutation.Mutant]]
    */
  final def apply(token: RegexTree): Seq[Mutant] = mutate(token)

  /** Mutate the given token
    * @param token
    *   Target token
    * @return
    *   Sequence of [[weaponregex.model.mutation.Mutant]]
    */
  def mutate(token: RegexTree): Seq[Mutant]

  /** Extension class for a mutated pattern string to convert it into a [[weaponregex.model.mutation.Mutant]] using the
    * information of the current token mutator
    * @param pattern
    *   The string pattern to be converted
    */
  implicit protected class MutatedPatternExtension(pattern: String) {

    /** Convert a mutated pattern string into a [[weaponregex.model.mutation.Mutant]] at the provided
      * [[weaponregex.model.Location]]
      * @param location
      *   [[weaponregex.model.Location]] of the mutation
      * @return
      *   A [[weaponregex.model.mutation.Mutant]]
      */
    def toMutantAt(location: Location): Mutant =
      Mutant(pattern, name, location, levels, description)

    /** Convert a mutated pattern string into a [[weaponregex.model.mutation.Mutant]] with the
      * [[weaponregex.model.Location]] taken from the provided token
      * @param token
      *   The token for reference
      * @return
      *   A [[weaponregex.model.mutation.Mutant]]
      */
    def toMutantOf(token: RegexTree): Mutant = toMutantAt(token.location)

    /** Convert a mutated pattern string into a [[weaponregex.model.mutation.Mutant]] with the
      * [[weaponregex.model.Location]] starts from the start of the provided token and ends at the start of the token's
      * first child
      *
      * If the given token has no child, the location of the given token is considered to be the location of the mutant
      * @param token
      *   The token for reference
      * @return
      *   A [[weaponregex.model.mutation.Mutant]]
      */
    def toMutantBeforeChildrenOf(token: RegexTree): Mutant = {
      val loc: Location =
        if (token.children.isEmpty) token.location
        else Location(token.location.start, token.children.head.location.start)
      toMutantAt(loc)
    }

    /** Convert a mutated pattern string into a [[weaponregex.model.mutation.Mutant]] with the
      * [[weaponregex.model.Location]] starts from the end of the provided token's last child and ends at the end of the
      * token
      *
      * If the given token has no child, the location of the given token is considered to be the location of the mutant
      * @param token
      *   The token for reference
      * @return
      *   A [[weaponregex.model.mutation.Mutant]]
      */
    def toMutantAfterChildrenOf(token: RegexTree): Mutant = {
      val loc: Location =
        if (token.children.isEmpty) token.location
        else Location(token.children.last.location.end, token.location.end)
      toMutantAt(loc)
    }
  }
}
