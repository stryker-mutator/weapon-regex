package weaponregex.parser

import scala.scalajs.js.annotation.*

/** A regex parser flavor */
sealed trait ParserFlavor

/** JVM regex parser flavor */
@JSExportTopLevel("ParserFlavorJVM")
case object ParserFlavorJVM extends ParserFlavor

/** JS regex parser flavor */
@JSExportTopLevel("ParserFlavorJS")
case object ParserFlavorJS extends ParserFlavor
