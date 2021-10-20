package weaponregex.model

import scala.scalajs.js.annotation._

/** A location in the source code which can span multiple lines and/or columns.
  *
  * @param start
  *   start [[weaponregex.model.Position]]
  * @param end
  *   end [[weaponregex.model.Position]]
  */
@JSExportAll
case class Location(start: Position, end: Position) {
  val pretty: String = s"[${start.pretty}, ${end.pretty})"
}

/** Companion object for [[weaponregex.model.Location]]
  */
object Location {
  def apply(startLine: Int, startColumn: Int)(endLine: Int, endColumn: Int): Location =
    Location(Position(startLine, startColumn), Position(endLine, endColumn))
}
