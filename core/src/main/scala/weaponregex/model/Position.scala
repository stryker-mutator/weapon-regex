package weaponregex.model

import scala.scalajs.js.annotation.*

/** A specific spot in the source code based on line and column. Stryker uses zero-based indexes. So the first character
  * in a file is at line 0, column 0.
  *
  * @param line
  *   line number
  * @param column
  *   column number
  */
@JSExportAll
case class Position(line: Int, column: Int) {
  override def toString: String = s"$line:$column"
}
