package weaponregex.model

import cats.parse.Caret
import cats.syntax.order.*
import cats.{Order, Show}
import weaponregex.model.Position.*

import scala.scalajs.js.annotation.*

/** A location in the source code which can span multiple lines and/or columns.
  *
  * @param start
  *   start [[weaponregex.model.Position]] (inclusive)
  * @param end
  *   end [[weaponregex.model.Position]] (exclusive)
  */
@JSExportAll
case class Location(start: Position, end: Position) {
  require(end >= start, s"Location end ${end.show} must be >= start ${start.show}")
  def show: String = s"[${start.show}, ${end.show})"
}

/** Companion object for [[weaponregex.model.Location]]
  */
object Location {
  def apply(startLine: Int, startColumn: Int)(endLine: Int, endColumn: Int): Location =
    Location(Position(startLine, startColumn), Position(endLine, endColumn))

  def fromCaret(start: Caret, end: Caret): Location =
    Location(Position(start.line, start.col), Position(end.line, end.col))

  implicit val showLocation: Show[Location] = _.show

  implicit val locationOrder: Order[Location] = Order.by(l => (l.start, l.end))

}
