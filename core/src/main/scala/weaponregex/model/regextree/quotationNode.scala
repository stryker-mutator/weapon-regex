package weaponregex.model.regextree

import weaponregex.model.Location

/** Quote for the following single character
  *
  * @param char
  *   The character being quoted
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class QuoteChar(char: Char, override val location: Location) extends Leaf(char, location, """\""")

/** Quote from \Q to an optional \E
  * @param quote
  *   The string being quoted
  * @param hasEnd
  *   `true` if quote has an end symbol `\E`, `false` otherwise
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Quote(quote: String, hasEnd: Boolean, override val location: Location)
    extends Leaf(quote, location, """\Q""", if (hasEnd) """\E""" else "")
