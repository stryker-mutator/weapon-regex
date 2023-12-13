package weaponregex.internal.constant

private[weaponregex] case object ErrorMessage {
  val errorHeader: String = "[Error]"
  val parserErrorHeader: String = s"$errorHeader Parser: "

  val unsupportedFlavor: String = parserErrorHeader + "Unsupported regex flavor"
  val jvmWithStringFlags: String = parserErrorHeader + "JVM regex flavor does not support string flags"
}
