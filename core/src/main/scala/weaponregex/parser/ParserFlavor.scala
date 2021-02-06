package weaponregex.parser

sealed trait ParserFlavor
case object ParserFlavorJVM extends ParserFlavor
case object ParserFlavorJS extends ParserFlavor
