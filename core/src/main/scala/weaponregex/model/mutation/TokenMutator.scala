package weaponregex.model.mutation

import weaponregex.model.Location
import weaponregex.model.regextree.RegexTree
import weaponregex.model.regextree.Node

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

  /** Provide a description for a specific mutation based on a given token and location. By default, it is the same as
    * the description of the mutator with the addition of the start position from the given location.
    *
    * @param token
    *   The token to be mutated
    * @param location
    *   The location of the mutation
    * @return
    *   A description of the mutation
    */
  def describeMutation(token: RegexTree, location: Location): String = s"$description at ${location.start}"

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
      * @param description
      *   A description of the mutation. By default, it is the same as the description of the mutator.
      * @return
      *   A [[weaponregex.model.mutation.Mutant]]
      */
    def toMutantAt(location: Location, description: String = description): Mutant =
      Mutant(pattern, name, location, levels, description)

    /** Convert a mutated pattern string into a [[weaponregex.model.mutation.Mutant]] with the
      * [[weaponregex.model.Location]] taken from the provided token
      * @param token
      *   The token for reference
      * @return
      *   A [[weaponregex.model.mutation.Mutant]]
      */
    def toMutantOf(token: RegexTree): Mutant =
      toMutantAt(token.location, describeMutation(token, token.location))

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
      val loc: Location = token match {
        case node: Node if node.children.nonEmpty => Location(token.location.start, node.children.head.location.start)
        case _                                    => token.location
      }
      toMutantAt(loc, describeMutation(token, loc))
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
      val loc: Location = token match {
        case node: Node if node.children.nonEmpty => Location(node.children.last.location.end, token.location.end)
        case _                                    => token.location
      }

      toMutantAt(loc, describeMutation(token, loc))
    }
  }
}
