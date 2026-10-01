package weaponregex.internal.parser

import cats.parse.{Numbers, Parser as P}
import weaponregex.internal.model.regextree.*

import java.lang.Character as JChar

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
private[weaponregex] class ParserJS private[parser] (unicodeMode: Boolean, singleLine: Boolean)
    extends Parser(singleLine) {

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
  override protected val backslashClassItem: P[RegexTree] =
    if (unicodeMode)
      P.oneOf(
        preDefinedCharClass.backtrack ::
          unicodeCharClass.backtrack ::
          P.defer(metaCharacter).backtrack ::
          quoteChar.backtrack :: Nil
      )
    else
      P.oneOf(
        preDefinedCharClass.backtrack ::
          P.defer(metaCharacter).backtrack ::
          quoteChar.backtrack :: Nil
      )

  /** Intermediate parsing rule for character class item tokens that do not start with a backslash
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    * @note
    *   Nested character class is a Scala/Java-only regex syntax
    */
  override protected val plainClassItem: P[RegexTree] =
    P.oneOf(range.backtrack :: charClassCharLiteral.backtrack :: Nil)

  /** Parse a group name. A name starts with an ID_Start character, `$` or `_`, followed by ID_Continue characters, `$`,
    * ZWNJ or ZWJ
    * @return
    *   the parsed name string
    * @example
    *   `"name1"`
    * @see
    *   [[https://tc39.es/ecma262/#prod-RegExpIdentifierName]]
    */
  override protected val groupName: P[String] = ParserJS.groupName

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
  override protected val backslashElementaryRE: P[RegexTree] =
    if (unicodeMode)
      P.oneOf(
        preDefinedCharClass.backtrack ::
          unicodeCharClass.backtrack ::
          boundaryMetaChar.backtrack ::
          reference.backtrack ::
          P.defer(metaCharacter).backtrack ::
          quote :: Nil
      )
    else
      P.oneOf(
        preDefinedCharClass.backtrack ::
          boundaryMetaChar.backtrack ::
          reference.backtrack ::
          P.defer(metaCharacter).backtrack ::
          quote :: Nil
      )

  /** Intermediate parsing rule which parses the alternatives that do not start with a backslash
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  override protected val plainElementaryRE: P[RegexTree] =
    P.oneOf(
      charLiteral.backtrack ::
        capturing ::
        anyDot ::
        bol ::
        eol ::
        charClass.backtrack :: Nil
    )
}

object ParserJS {

  /** Whether the flags contain the `u` or `v` flag for Unicode mode */
  private def unicodeMode(flags: Option[String]): Boolean = flags.exists(f => f.contains('u') || f.contains('v'))

  /** A surrogate pair, parsed as the supplementary code point it encodes */
  private val surrogatePair: P[Int] =
    (P.charIn('\ud800' to '\udbff') ~ P.charIn('\udc00' to '\udfff')).map { case (high, low) =>
      JChar.toCodePoint(high, low)
    }

  /** A Unicode escape in a group name, parsed as the code point it encodes: `\ uXXXX`, `\ u{X...}`, or a surrogate pair
    * escaped as `\ uXXXX\ uXXXX`. Group names always allow these escapes, also outside of Unicode mode
    * @see
    *   [[https://tc39.es/ecma262/#prod-RegExpUnicodeEscapeSequence]]
    */
  // `.filter()` function from cats-parse is wrongly mutated by Stryker4s into `.filterNot()` which does not exist in cats-parse
  @SuppressWarnings(Array("stryker4s.mutation.MethodExpression"))
  private val unicodeEscape: P[Int] = {
    val hex4: P[Char] = Parser.hex4Digits.map(Integer.parseInt(_, 16).toChar)
    // A high surrogate may be followed by an escaped low surrogate; together they encode a supplementary code point
    val lowSurrogate: P[Char] = (P.string("\\u") *> hex4.filter(JChar.isLowSurrogate)).backtrack
    val hex4CodePoint: P[Int] = hex4.flatMap { c =>
      if (JChar.isHighSurrogate(c)) lowSurrogate.?.map(_.fold(c.toInt)(JChar.toCodePoint(c, _)))
      else P.pure(c.toInt)
    }
    P.string("\\u") *> (Parser.bracedHexDigits.mapFilter(Parser.codePoint) | hex4CodePoint)
  }

  /** Parse a single code point that satisfies the given predicate. A supplementary code point is parsed as its
    * surrogate pair, and any code point can be written as a [[unicodeEscape]]
    * @param isValid
    *   The predicate the code point must satisfy
    */
  // `.filter()` function from cats-parse is wrongly mutated by Stryker4s into `.filterNot()` which does not exist in cats-parse
  @SuppressWarnings(Array("stryker4s.mutation.MethodExpression"))
  private def codePointWhere(isValid: Int => Boolean): P[Unit] =
    P.charWhere(c => isValid(c.toInt)).void | (surrogatePair | unicodeEscape).filter(isValid).backtrack.void

  /** Group name parser shared by all instances */
  private val groupName: P[String] = {
    val start = codePointWhere(cp => cp == '$' || cp == '_' || JChar.isUnicodeIdentifierStart(cp))
    val part = codePointWhere(cp =>
      cp == '$' || cp == '\u200c' || cp == '\u200d' ||
        (JChar.isUnicodeIdentifierPart(cp) && !JChar.isIdentifierIgnorable(cp))
    )
    (start ~ part.rep0).string
  }

  // Create lazy instances so all parsers are only instantiated once
  private lazy val unicodeParser: ParserJS = new ParserJS(true, false)
  private lazy val nonUnicodeParser: ParserJS = new ParserJS(false, false)
  private lazy val unicodeSingleLineParser: ParserJS = new ParserJS(true, true)
  private lazy val nonUnicodeSingleLineParser: ParserJS = new ParserJS(false, true)

  private[parser] def apply(flags: Option[String] = None, singleLine: Boolean = false): ParserJS =
    if (unicodeMode(flags)) { if (singleLine) unicodeSingleLineParser else unicodeParser }
    else { if (singleLine) nonUnicodeSingleLineParser else nonUnicodeParser }
}
