package weaponregex.parser

import fastparse._
import NoWhitespace._
import weaponregex.model.regextree.{MetaChar, RegexTree}

/** Concrete parser for JVM flavor of regex
  * @param pattern The regex pattern to be parsed
  * @note This class constructor is private, instances must be created using the companion [[weaponregex.parser.Parser]] object
  * @see [[https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html]]
  */
class ParserJVM private[parser] (pattern: String) extends Parser(pattern) {

  /** Regex special characters
    */
  override val specialChars: String = """()[{\.^$|?*+"""

  /** Special characters within a character class
    */
  override val charClassSpecialChars: String = """[]\"""

  /** Allowed boundary meta-characters
    */
  override val boundaryMetaChars: String = "bBAGzZ"

  /** Allowed escape characters
    */
  override val escapeChars: String = "\\\\tnrfae" // fastparse needs `////` for a single backslash

  /** Allowed predefined character class characters
    */
  override val predefCharClassChars: String = "dDhHsSvVwW"

  /** Parse a character with octal value `\0n`, `\0nn`, `\0mnn` (0 <= m <= 3, 0 <= n <= 7)
    *
    * @return [[weaponregex.model.regextree.MetaChar]] tree node
    * @example `"\012"`
    */
  override def charOct[_: P]: P[MetaChar] =
    Indexed("""\0""" ~ (CharIn("0-3") ~ CharIn("0-7").rep(exactly = 2) | CharIn("0-7").rep(min = 1, max = 2)).!)
      .map { case (loc, octDigits) => MetaChar("0" + octDigits, loc) }

  /** Intermediate parsing rule for special construct tokens which can parse either `namedGroup`, `nonCapturingGroup`, `flagToggleGroup`, `flagNCGroup`, `lookaround` or `atomicGroup`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  override def specialConstruct[_: P]: P[RegexTree] = P(
    namedGroup | nonCapturingGroup | flagToggleGroup | flagNCGroup | lookaround | atomicGroup
  )
}
