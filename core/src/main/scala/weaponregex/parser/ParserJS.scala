package weaponregex.parser

class ParserJS(pattern: String) extends Parser(pattern) {

  /** Regex special characters
    */
  override val specialChars: String = """()[\.^$|?*+"""
}
