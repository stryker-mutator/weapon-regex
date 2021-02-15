package weaponregex.parser

import fastparse._
import NoWhitespace._
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
  override val escapeChars: String = "\\\\tnrf" // fastparse needs `////` for a single backslash

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

  /** Intermediate parsing rule for quoting tokens which can parse only `quoteChar`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  override def quote[_: P]: P[RegexTree] = quoteChar

  /** Parse a character with octal value `\n`, `\nn`, `\mnn` (0 <= m,n <= 9)
    *
    * @return [[weaponregex.model.regextree.MetaChar]] tree node
    * @example `"\012"`
    * @note This syntax will correctly match if 0 <= m <= 3, 0 <= n <= 7; but m and/or n outside of this range will still be parsable.
    */
  override def charOct[_: P]: P[MetaChar] = Indexed("""\""" ~ CharIn("0-9").rep(min = 1, max = 3).!)
    .map { case (loc, octDigits) => MetaChar(octDigits, loc) }

  /** Intermediate parsing rule for reference tokens which can parse only `nameReference`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  override def reference[_: P]: P[RegexTree] = nameReference
}
