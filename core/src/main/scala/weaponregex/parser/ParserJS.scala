package weaponregex.parser

/** Concrete parser for JS flavor of regex
  * @param pattern The regex pattern to be parsed
  * @note This class constructor is private, instances must be created using the companion [[weaponregex.parser.Parser]] object
  * @see [[https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions/Cheatsheet]]
  */
class ParserJS private[parser] (pattern: String) extends Parser(pattern) {

  /** Regex special characters
    */
  override val specialChars: String = """()[\.^$|?*+"""

  /** Allowed boundary meta-characters
    */
  override val boundaryMetaChars: String = "bB"

  /** Allowed escape characters
    */
  override val escapeChars: String = "\\\\tnrf"

  /** Allowed predefined character class characters
    */
  override val predefCharClassChars: String = "dDsSvwW"
}
