package weaponregex.internal.constant

private[weaponregex] case object ErrorMessage {
  private val parserErrorHeader: String = s"[Error] Parser: "

  val unsupportedFlavor: String = parserErrorHeader + "Unsupported regex flavor"
  val jvmWithStringFlags: String = parserErrorHeader + "JVM regex flavor does not support string flags"
}
