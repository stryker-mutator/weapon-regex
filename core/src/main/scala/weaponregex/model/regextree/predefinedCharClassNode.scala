package weaponregex.model.regextree

import weaponregex.model.Location

/** Predefined character class leaf node
  *
  * @param charClass
  *   The literal class character without the `\`
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class PredefinedCharClass(charClass: String, override val location: Location)
    extends Leaf(charClass, location, """\""")

/** Unicode character class leaf node
  * @param property
  *   The class character property
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class UnicodeCharClass(property: String, override val location: Location, isPositive: Boolean = true)
    extends Leaf(property, location, if (isPositive) """\p{""" else """\P{""", "}")
