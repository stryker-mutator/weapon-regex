package weaponregex.parser

import fastparse.*
import NoWhitespace.*
import weaponregex.model.regextree.*

/** Concrete parser for JS flavor of regex
  * @param pattern
  *   The regex pattern to be parsed
  * @param flags
  *   The regex flags to be used
  * @note
  *   This class constructor is private, instances must be created using the companion [[weaponregex.parser.Parser]]
  *   object
  * @see
  *   [[https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions/Cheatsheet]]
  * @see
  *   [[https://tc39.es/ecma262/multipage/text-processing.html#sec-patterns]]
  */
class ParserJS private[parser] (pattern: String, val flags: Option[String] = None) extends Parser(pattern) {

  /** Whether the flags contain the `u` flag for Unicode mode */
  private val unicodeMode: Boolean = flags.contains("u")

  /** Regex special characters
    */
  override val specialChars: String = """()[{\.^$|?*+"""

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

  /** Minimum number of character class items of a valid character class
    */
  override val minCharClassItem: Int = 0

  /** The escape character used with a code point
    * @example
    *   `\ x{h..h}` or `\ u{h..h}`
    */
  override val codePointEscChar: String = "u"

  /** Parse special cases of a character literal
    * @return
    *   The captured character as a string
    */
  override def charLiteralSpecialCases[A: P]: P[String] = P("{".! ~ !quantifierLongTail)

  /** Intermediate parsing rule for character class item tokens which can parse either `preDefinedCharClass`,
    * `metaCharacter`, `range`, `quoteChar`, or `charClassCharLiteral`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    * @note
    *   Nested character class is a Scala/Java-only regex syntax
    */
  override def classItem[A: P]: P[RegexTree] =
    if (unicodeMode) P(preDefinedCharClass | posixCharClass | metaCharacter | range | quoteChar | charClassCharLiteral)
    else P(preDefinedCharClass | metaCharacter | range | quoteChar | charClassCharLiteral)

  /** Parse a quoted character (any character). If [[weaponregex.parser.ParserJS unicodeMode]] is true, only the
    * following characters are allowed: `^ $ \ . * + ? ( ) [ ] { } |` or `/`
    * @return
    *   [[weaponregex.model.regextree.QuoteChar]]
    * @example
    *   `"\$"`
    */
  override def quote[A: P]: P[QuoteChar] = if (unicodeMode)
    Indexed("""\""" ~ CharIn("""^$\.*+?()[]{}|/""").!)
      .map { case (loc, char) => QuoteChar(char.head, loc) }
  else quoteChar

  /** Parse a character with octal value `\n`, `\nn`, `\mnn` (0 <= m,n <= 9)
    *
    * @return
    *   [[weaponregex.model.regextree.MetaChar]] tree node
    * @example
    *   `"\012"`
    * @note
    *   This syntax will correctly match if 0 <= m <= 3, 0 <= n <= 7; but m and/or n outside of this range will still be
    *   parsable.
    */
  override def charOct[A: P]: P[MetaChar] = Indexed("""\""" ~ CharIn("0-9").rep(min = 1, max = 3).!)
    .map { case (loc, octDigits) => MetaChar(octDigits, loc) }

  /** Intermediate parsing rule for reference tokens which can parse only `nameReference`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  override def reference[A: P]: P[RegexTree] = nameReference

  /** Intermediate parsing rule for meta-character tokens which can parse either `charOct`, `charHex`, `charUnicode` or
    * `escapeChar`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  override def metaCharacter[A: P]: P[RegexTree] =
    if (unicodeMode) P(charOct | charHex | charUnicode | charCodePoint | escapeChar | controlChar)
    else P(charOct | charHex | escapeChar | controlChar)

  /** Intermediate parsing rule which can parse either `capturing`, `anyDot`, `preDefinedCharClass`, `boundary`,
    * `charClass`, `reference`, `character` or `quote`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  override def elementaryRE[A: P]: P[RegexTree] =
    if (unicodeMode)
      P(
        capturing | anyDot | preDefinedCharClass | posixCharClass | boundary | charClass | reference | character | quote
      )
    else P(capturing | anyDot | preDefinedCharClass | boundary | charClass | reference | character | quote)
}
