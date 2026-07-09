package weaponregex.internal.parser

import cats.parse.Rfc5234.*
import cats.parse.{Numbers, Parser as P}
import weaponregex.internal.model.regextree.*

/** Parser instance for JS flavor of regex
  * @param unicodeMode
  *   Whether the parser should be in Unicode mode, which is determined by the presence of `u` or `v` flag.
  * @note
  *   This class constructor is private, instances must be created using the companion
  *   [[weaponregex.internal.parser.ParserJS]] object
  * @see
  *   [[https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions/Cheatsheet]]
  * @see
  *   [[https://tc39.es/ecma262/multipage/text-processing.html#sec-patterns]]
  */
private[weaponregex] class ParserJS private[parser] (unicodeMode: Boolean) extends Parser {

  /** Regex special characters
    */
  override protected val specialChars: String = """()[{\.^$|?*+"""

  /** Special characters within a character class
    */
  override protected val charClassSpecialChars: String = """]\"""

  /** Allowed boundary meta-characters
    */
  override protected val boundaryMetaChars: String = "bB"

  /** Allowed escape characters
    */
  override protected val escapeChars: String = "\\\\tnrf" // need `////` for a single backslash

  /** Allowed predefined character class characters
    */
  override protected val predefCharClassChars: String = "dDsSvwW"

  /** Minimum number of character class items of a valid character class
    */
  override protected val minCharClassItem: Int = 0

  /** The escape character used with a code point
    * @example
    *   `\ x{h..h}` or `\ u{h..h}`
    */
  override protected val codePointEscChar: Char = 'u'

  /** Parse special cases of a character literal
    * @return
    *   The captured character as a string
    */
  override protected val charLiteralSpecialCases: P[Char] =
    P.char('{').as('{') <* P.not(quantifierLongTail)

  /** Intermediate parsing rule for character class item tokens which can parse either `preDefinedCharClass`,
    * `metaCharacter`, `range`, `quoteChar`, or `charClassCharLiteral`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    * @note
    *   Nested character class is a Scala/Java-only regex syntax
    */
  override protected val classItem: P[RegexTree] =
    if (unicodeMode)
      P.oneOf(
        range.backtrack ::
          charClassCharLiteral.backtrack ::
          preDefinedCharClass.backtrack ::
          unicodeCharClass.backtrack ::
          P.defer(metaCharacter).backtrack ::
          quoteChar.backtrack :: Nil
      )
    else
      P.oneOf(
        range.backtrack ::
          charClassCharLiteral.backtrack ::
          preDefinedCharClass.backtrack ::
          P.defer(metaCharacter).backtrack ::
          quoteChar.backtrack :: Nil
      )

  /** Parse a group name
    * @return
    *   the parsed name string
    * @example
    *   `"name1"`
    */
  override protected val groupName: P[String] =
    ((alpha.void | P.char('_')) ~
      (alpha.void | digit.void | P.char('_')).rep).string

  /** Parse a quoted character (any character). If [[weaponregex.internal.parser.ParserJS unicodeMode]] is true, only
    * the following characters are allowed: `^ $ \ . * + ? ( ) [ ] { } |` or `/`
    * @return
    *   [[weaponregex.internal.model.regextree.QuoteChar]]
    * @example
    *   `"\$"`
    */
  override protected val quote: P[RegexTree] =
    if (unicodeMode)
      indexed(P.char('\\') *> P.charIn("""^$\.*+?()[]{}|/"""))
        .map { case (loc, char) => QuoteChar(char, loc) }
        .withContext("quoted character")
    else quoteChar

  /** Parse a character with octal value `\n`, `\nn`, `\mnn` (0 <= m,n <= 9)
    *
    * @return
    *   [[weaponregex.internal.model.regextree.MetaChar]] tree node
    * @example
    *   `"\012"`
    * @note
    *   This syntax will correctly match if 0 <= m <= 3, 0 <= n <= 7; but m and/or n outside of this range will still be
    *   parsable.
    */
  override protected val charOct: P[MetaChar] =
    indexed(P.char('\\') *> Numbers.digit.rep(1, 3).string)
      .map { case (loc, octDigits) => MetaChar(octDigits, loc) }
      .withContext("octal character")

  /** Intermediate parsing rule for reference tokens which can parse only `nameReference`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  override protected val reference: P[RegexTree] = nameReference

  /** Intermediate parsing rule for meta-character tokens which can parse either `charOct`, `charHex`, `charUnicode` or
    * `escapeChar`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  override protected val metaCharacter: P[RegexTree] =
    if (unicodeMode)
      P.oneOf(
        charOct.backtrack ::
          charHex.backtrack ::
          charUnicode.backtrack ::
          charCodePoint.backtrack ::
          escapeChar.backtrack ::
          controlChar.backtrack :: Nil
      )
    else P.oneOf(charOct.backtrack :: charHex.backtrack :: escapeChar.backtrack :: controlChar.backtrack :: Nil)

  /** Intermediate parsing rule which can parse either `capturing`, `anyDot`, `preDefinedCharClass`, `boundary`,
    * `charClass`, `reference`, `character` or `quote`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  override protected val elementaryRE: P[RegexTree] =
    if (unicodeMode)
      P.oneOf(
        charLiteral.backtrack ::
          capturing ::
          anyDot ::
          preDefinedCharClass.backtrack ::
          unicodeCharClass.backtrack ::
          boundary ::
          charClass.backtrack ::
          reference.backtrack ::
          character.backtrack ::
          quote :: Nil
      )
    else
      P.oneOf(
        charLiteral.backtrack ::
          capturing ::
          anyDot ::
          preDefinedCharClass.backtrack ::
          boundary ::
          charClass.backtrack ::
          reference.backtrack ::
          character.backtrack ::
          quote :: Nil
      )
}

object ParserJS {

  /** Whether the flags contain the `u` or `v` flag for Unicode mode */
  private def unicodeMode(flags: Option[String]): Boolean = flags.exists(f => f.contains('u') || f.contains('v'))

  // Create lazy instances so all parsers are only instantiated once
  private lazy val unicodeParser: ParserJS = new ParserJS(true)
  private lazy val nonUnicodeParser: ParserJS = new ParserJS(false)

  private[parser] def apply(flags: Option[String] = None): ParserJS =
    if (unicodeMode(flags)) unicodeParser else nonUnicodeParser
}
