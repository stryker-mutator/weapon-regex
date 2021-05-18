package weaponregex.model.regextree

import weaponregex.model.Location

/** The leaf of the [[weaponregex.model.regextree.RegexTree]] (terminal node) that have no children node
  * @param value The value that the leaf holds
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  * @param prefix The string that is put in front of the leaf's value when building
  * @param postfix The string that is put after the leaf's value when building
  */
abstract class Leaf[A](
    val value: A,
    override val location: Location,
    override val prefix: String = "",
    override val postfix: String = ""
) extends RegexTree {

  /** Since leaves have no children, this is always empty
    */
  final override val children: Seq[RegexTree] = Nil

  /** Since leaves have no children, this is always an empty string
    */
  final override val sep: String = ""
}

/** Character literal leaf node
  * @param char The character literal value
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Character(char: Char, override val location: Location) extends Leaf(char, location)

/** Any dot `.` predefined character class leaf node
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  * @note This is technically a predefined character class, but because it cannot be negated nor has a `\` prefix, it is handled separately here
  */
case class AnyDot(override val location: Location) extends Leaf('.', location)

/** Meta-characters leaf node
  * @param metaChar Can be any meta character as defined in the grammar
  * @param location [[weaponregex.model.Location]] of the token in the regex string
  */
case class MetaChar(metaChar: String, override val location: Location) extends Leaf(metaChar, location, """\""")

/** Control meta-characters leaf node corresponding to a given character
  * @param controlChar Any character in a-z or A-Z
  * @param location [[weaponregex.model.Location]] of the token in the regex string
  * @note This is technically a meta-character, but because it has an additional target character and a `\c` prefix, it is handled separately here
  */
case class ControlChar(controlChar: String, override val location: Location)
    extends Leaf(controlChar, location, """\c""")

/** Predefined character class leaf node
  * @param charClass The literal class character without the `\`
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class PredefinedCharClass(charClass: String, override val location: Location)
    extends Leaf(charClass, location, """\""")

/** POSIX character class leaf node
  * @param property The class character property
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class POSIXCharClass(property: String, override val location: Location, isPositive: Boolean = true)
    extends Leaf(property, location, if (isPositive) """\p{""" else """\P{""", "}")

/** Beginning of Line (BOL) leaf node
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class BOL(override val location: Location) extends Leaf('^', location)

/** End of Line (EOL) leaf node
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class EOL(override val location: Location) extends Leaf('$', location)

/** Boundary meta character leaf node
  * @param boundary The literal boundary character without the `\`
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Boundary(boundary: String, override val location: Location) extends Leaf(boundary, location, """\""")

/** Reference to a named capturing group leaf node
  * @param name The name of the capturing group being referenced
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class NameReference(name: String, override val location: Location) extends Leaf(name, location, """\k<""", ">")

/** Reference to a numbered capturing group leaf node
  * @param num The order number of the capturing group being referenced
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class NumberReference(num: Int, override val location: Location) extends Leaf(num, location, """\""")

/** Quote for the following single character
  * @param char The character being quoted
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class QuoteChar(char: Char, override val location: Location) extends Leaf(char, location, """\""")

/** Quote from \Q to an optional \E
  * @param quote The string being quoted
  * @param hasEnd `true` if quote has an end symbol `\E`, `false` otherwise
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Quote(quote: String, hasEnd: Boolean, override val location: Location)
    extends Leaf(quote, location, """\Q""", if (hasEnd) """\E""" else "")

/** Empty string (nothing, null) leaf
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Empty(override val location: Location) extends Leaf("", location)
