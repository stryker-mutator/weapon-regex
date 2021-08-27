package weaponregex.extension

import fastparse.internal.Util
import weaponregex.model._

object StringExtension {
  implicit class StringIndexExtension(string: String) {

    /** Convert an index of into row and column numbers in the given string.
      *
      * @param index
      *   An index
      * @return
      *   A tuple of row and column numbers
      *
      * @note
      *   This function implementation is taken from
      *   [[https://github.com/lihaoyi/fastparse/blob/master/fastparse/src/fastparse/ParserInput.scala here]]
      */
    final def toLineCol(index: Int): (Int, Int) = {
      val lineNumberLookup = Util.lineNumberLookup(string)
      val line = lineNumberLookup.indexWhere(_ > index) match {
        case -1 => lineNumberLookup.length - 1
        case n  => math.max(0, n - 1)
      }
      val col = index - lineNumberLookup(line)
      (line, col)
    }

    /** Convert an index into a [[weaponregex.model.Position]] in the given string.
      * @param index
      *   An index
      * @return
      *   A [[weaponregex.model.Position]]
      */
    final def positionOf(index: Int): Position = {
      val (line, column) = string toLineCol index
      Position(line, column)
    }

    /** Convert a pair of start and end indices into a [[weaponregex.model.Location]] in the given string.
      * @param start
      *   Start index
      * @param end
      *   End index
      * @return
      *   A [[weaponregex.model.Location]]
      */
    final def locationOf(start: Int, end: Int): Location = Location(string positionOf start, string positionOf end)
  }

  implicit class StringStylingExtension(string: String) {

    /** Character-wise toggling the case of a String
      * @return
      *   Case-toggled String
      */
    final def toggleCase: String = string map (char => if (char.isUpper) char.toLower else char.toUpper)
  }
}
