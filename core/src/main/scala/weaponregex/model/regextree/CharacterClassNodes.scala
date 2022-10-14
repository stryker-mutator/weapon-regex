package weaponregex.model.regextree

import weaponregex.model.Location

/** Character class node
  *
  * @param nodes
  *   The child nodes contained in the character class
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  * @param isPositive
  *   `true` if the character class is positive, `false` otherwise
  */
case class CharacterClass(nodes: Seq[RegexTree], override val location: Location, isPositive: Boolean = true)
    extends Node(nodes, location, if (isPositive) "[" else "[^", "]")

/** Character class node without the surround syntactical symbols, i.e. "naked"
  * @param nodes
  *   The child nodes contained in the character class
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class CharacterClassNaked(nodes: Seq[RegexTree], override val location: Location) extends Node(nodes, location)

/** Character class intersection used inside a character class
  * @param nodes
  *   The nodes that are being "or-ed"
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class CharClassIntersection(nodes: Seq[RegexTree], override val location: Location)
    extends Node(nodes, location, sep = "&&")

/** Character range that is used inside of a character class
  * @param from
  *   The left bound of the range
  * @param to
  *   The right bound of the range
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Range(from: Character, to: Character, override val location: Location)
    extends Node(Seq(from, to), location, sep = "-")
