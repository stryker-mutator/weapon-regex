package weaponregex.parser

class ParserJVM(pattern: String) extends Parser(pattern) {

  /** Regex special characters
    */
  override val specialChars: String = """()[{\.^$|?*+"""
}
