package weaponregex.model.mutation

import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.model.Location
import weaponregex.model.regextree.{Node, RegexTree}

trait TokenMutator {
  self =>

  /** The name of the mutator
    */
  val name: String

  /** The mutation levels that the token mutator falls under
    */
  val levels: Seq[Int]

  /** Generate the default description for the mutants of this mutator
    * @param original
    *   The original token string being mutated
    * @param mutated
    *   The mutated string
    * @param location
    *   The [[weaponregex.model.Location]] where the mutation occurred
    */
  def description(original: String, mutated: String, location: Location): String =
    s"${location.show} Mutate $original to $mutated"

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

    /** Convert a mutated pattern string into a [[weaponregex.model.mutation.Mutant]] with the
      * [[weaponregex.model.Location]] taken from the provided token
      * @param token
      *   The token for reference
      * @param location
      *   The [[weaponregex.model.Location]] where the mutation occurred. If not provided, this will be taken from the
      *   provided token.
      * @param description
      *   The description of the mutant. If not provided, the default description of the mutator is used instead.
      * @return
      *   A [[weaponregex.model.mutation.Mutant]]
      */
    def toMutantOf(token: RegexTree, location: Option[Location] = None, description: Option[String] = None): Mutant = {
      val loc: Location = location.getOrElse(token.location)
      val desc: String = description.getOrElse(self.description(token.build, pattern, loc))
      Mutant(pattern, name, loc, levels, desc)
    }

    /** Convert a mutated pattern string into a [[weaponregex.model.mutation.Mutant]] with the
      * [[weaponregex.model.Location]] starts from the start of the provided token and ends at the start of the token's
      * first child
      *
      * If the given token has no child, the location of the given token is considered to be the location of the mutant
      * @param token
      *   The token for reference
      * @param description
      *   The description of the mutant. If not provided, the default description of the mutator is used instead.
      * @return
      *   A [[weaponregex.model.mutation.Mutant]]
      */
    def toMutantBeforeChildrenOf(token: RegexTree, description: Option[String] = None): Mutant = {
      val loc: Location = token match {
        case node: Node if node.children.nonEmpty => Location(token.location.start, node.children.head.location.start)
        case _                                    => token.location
      }
      toMutantOf(token, Some(loc), description)
    }

    /** Convert a mutated pattern string into a [[weaponregex.model.mutation.Mutant]] with the
      * [[weaponregex.model.Location]] starts from the end of the provided token's last child and ends at the end of the
      * token
      *
      * If the given token has no child, the location of the given token is considered to be the location of the mutant
      * @param token
      *   The token for reference
      * @param description
      *   The description of the mutant. If not provided, the default description of the mutator is used instead.
      * @return
      *   A [[weaponregex.model.mutation.Mutant]]
      */
    def toMutantAfterChildrenOf(token: RegexTree, description: Option[String] = None): Mutant = {
      val loc: Location = token match {
        case node: Node if node.children.nonEmpty => Location(node.children.last.location.end, token.location.end)
        case _                                    => token.location
      }
      toMutantOf(token, Some(loc), description)
    }
  }
}
