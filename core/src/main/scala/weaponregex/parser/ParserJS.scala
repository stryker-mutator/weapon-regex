package weaponregex.parser

import fastparse._
import weaponregex.model.regextree._

/** Concrete parser for JS flavor of regex
  * @param pattern The regex pattern to be parsed
  * @note This class constructor is private, instances must be created using the companion [[weaponregex.parser.Parser]] object
  * @see [[https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions/Cheatsheet]]
  */
class ParserJS private[parser] (pattern: String) extends Parser(pattern) {

  /** Regex special characters
    */
  override val specialChars: String = """()[\.^$|?*+"""

  /** Special characters within a character class
    */
  override val charClassSpecialChars: String = """]\"""

  /** Allowed boundary meta-characters
    */
  override val boundaryMetaChars: String = "bB"

  /** Allowed escape characters
    */
  override val escapeChars: String = "\\\\tnrf"

  /** Allowed predefined character class characters
    */
  override val predefCharClassChars: String = "dDsSvwW"

  /** Intermediate parsing rule for character class item tokens which can parse either `preDefinedCharClass`, `metaCharacter`, `range`, `quoteChar`, or `charClassCharLiteral`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    * @note Nested character class is a Scala/Java-only regex syntax
    */
  override def classItem[_: P]: P[RegexTree] = P(
    preDefinedCharClass | metaCharacter | range | quoteChar | charClassCharLiteral
  )
}
