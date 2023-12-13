package weaponregex.internal.model.regextree

import weaponregex.model.Location

/** Character literal leaf node
  *
  * @param char
  *   The character literal value
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Character(char: Char, override val location: Location) extends Leaf(char, location)

/** Any dot `.` predefined character class leaf node
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  * @note
  *   This is technically a predefined character class, but because it cannot be negated nor has a `\` prefix, it is
  *   handled separately here
  */
case class AnyDot(override val location: Location) extends Leaf('.', location)

/** Meta-characters leaf node
  * @param metaChar
  *   Can be any meta character as defined in the grammar
  * @param location
  *   [[weaponregex.model.Location]] of the token in the regex string
  */
case class MetaChar(metaChar: String, override val location: Location) extends Leaf(metaChar, location, """\""")

/** Control meta-characters leaf node corresponding to a given character
  * @param controlChar
  *   Any character in a-z or A-Z
  * @param location
  *   [[weaponregex.model.Location]] of the token in the regex string
  * @note
  *   This is technically a meta-character, but because it has an additional target character and a `\c` prefix, it is
  *   handled separately here
  */
case class ControlChar(controlChar: String, override val location: Location)
    extends Leaf(controlChar, location, """\c""")

/** Empty string (nothing, null) leaf
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Empty(override val location: Location) extends Leaf("", location)
