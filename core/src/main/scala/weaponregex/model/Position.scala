package weaponregex.model

import cats.{Order, Show}

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
  def show: String = s"${line}:${column}"
}

object Position {
  implicit val showPosition: Show[Position] = _.show

  implicit val positionOrder: Order[Position] = Order.by(p => (p.line, p.column))
}
