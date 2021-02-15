package weaponregex.parser

import scala.scalajs.js.annotation._

sealed trait ParserFlavor

@JSExportTopLevel("ParserFlavorJVM")
case object ParserFlavorJVM extends ParserFlavor

@JSExportTopLevel("ParserFlavorJS")
case object ParserFlavorJS extends ParserFlavor
