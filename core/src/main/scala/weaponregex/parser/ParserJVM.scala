package weaponregex.parser

/** Concrete parser for JVM flavor of regex
  * @param pattern The regex pattern to be parsed
  * @note This class constructor is private, instances must be created using the companion [[weaponregex.parser.Parser]] object
  * @see [[https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html]]
  */
class ParserJVM private[parser] (pattern: String) extends Parser(pattern) {

  /** Regex special characters
    */
  override val specialChars: String = """()[{\.^$|?*+"""

  /** Allowed boundary meta-characters
    */
  override val boundaryMetaChars: String = "bBAGzZ"

  /** Allowed escape characters
    */
  override val escapeChars: String = "\\\\tnrfae"

  /** Allowed predefined character class characters
    */
  override val predefCharClassChars: String = "dDhHsSvVwW"
}
