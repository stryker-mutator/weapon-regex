package weaponregex.internal.parser

import cats.parse.Rfc5234.*
import cats.parse.{Caret, Numbers, Parser as P, Parser0 as P0}
import cats.syntax.show.*
import mutationtesting.{Location, Position}
import weaponregex.internal.constant.ErrorMessage
import weaponregex.internal.model.regextree.*
import weaponregex.parser.*

/** Companion object for [[weaponregex.internal.parser.Parser]] class that instantiates flavor-specific parsers
  * instances
  */
private[weaponregex] object Parser {

  /** Apply the parser to parse the given pattern and flags
    * @param pattern
    *   The regex pattern to be parsed
    * @param flags
    *   The regex flags to be used
    * @return
    *   A `Right` of parsed [[weaponregex.internal.model.regextree.RegexTree]] if can be parsed, a `Left` with the error
    *   message otherwise
    */
  def apply(pattern: String, flags: Option[String], flavor: ParserFlavor): Either[String, RegexTree] =
    flavor match {
      case ParserFlavorJVM =>
        if (flags.isDefined) Left(ErrorMessage.jvmWithStringFlags)
        else ParserJVM.parse(pattern)
      case ParserFlavorJS => ParserJS(flags).parse(pattern)
      case null           => Left(ErrorMessage.unsupportedFlavor)
    }

  /** Apply the parser to parse the given pattern
    * @param pattern
    *   The regex pattern to be parsed
    * @return
    *   A `Right` of parsed [[weaponregex.internal.model.regextree.RegexTree]] if can be parsed, a `Left` with the error
    *   message otherwise
    */
  def apply(pattern: String, flavor: ParserFlavor = ParserFlavorJVM): Either[String, RegexTree] =
    apply(pattern, None, flavor)
}

/** The based abstract parser
  * @note
  *   The parsing rules methods inside this class is created based on the defined grammar
  */
abstract private[weaponregex] class Parser {

  /** Regex special characters
    */
  protected val specialChars: String

  /** Special characters within a character class
    */
  protected val charClassSpecialChars: String

  /** Allowed boundary meta-characters
    */
  protected val boundaryMetaChars: String

  /** Allowed escape characters
    */
  protected val escapeChars: String

  /** Allowed predefined character class characters
    */
  protected val predefCharClassChars: String

  /** Minimum number of character class items of a valid character class
    */
  protected val minCharClassItem: Int

  /** The escape character used with a code point
    * @example
    *   `\ x{h..h}` or `\ u{h..h}`
    */
  protected val codePointEscChar: Char

  protected def fromCaret(start: Caret, end: Caret): Location =
    Location(Position(start.line, start.col), Position(end.line, end.col))

  /** A higher order parser that add `mutationtesting.Location` index information of the parse of the given parser
    * @param p
    *   the parser to be indexed
    * @return
    *   A tuple of the `mutationtesting.Location` of the parse, and the return of the given parser `p`
    */
  protected def indexed[A](p: P[A]): P[(Location, A)] =
    (P.caret.with1 ~ p ~ P.caret).map { case ((i, a), j) => (fromCaret(i, j), a) }

  /** A higher order parser that add `mutationtesting.Location` index information of the parse of the given parser
    * @param p
    *   the parser to be indexed
    * @return
    *   A tuple of the `mutationtesting.Location` of the parse, and the return of the given parser `p`
    */
  protected def indexed0[A](p: P0[A]): P0[(Location, A)] =
    (P.caret ~ p ~ P.caret).map { case ((i, a), j) => (fromCaret(i, j), a) }

  protected val backslash: P[Unit] = P.char('\\')

  /** Parse an integer with any number of digits between 0 and 9
    * @return
    *   the parsed integer
    * @example
    *   `"123"`
    */
  protected val number: P[Int] = Numbers.nonNegativeIntString.map(_.toInt)

  /** Parse special cases of a character literal
    * @return
    *   The captured character as a string
    */
  protected val charLiteralSpecialCases: P[Char] = P.fail

  /** Parse a single literal character that is not a regex special character
    * @return
    *   [[weaponregex.internal.model.regextree.Character]] tree node
    * @example
    *   `"a"`
    * @see
    *   [[weaponregex.internal.parser.Parser.specialChars]]
    */
  protected val charLiteral: P[Character] =
    indexed(P.defer(P.charWhere(c => !specialChars.contains(c)) | charLiteralSpecialCases))
      .map { case (loc, c) => Character(c, loc) }
      .withContext("character literal")

  /** Intermediate parsing rule for character-related tokens which can parse either `metaCharacter` or `charLiteral`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  protected val character: P[RegexTree] = P.defer(metaCharacter.backtrack) | charLiteral

  /** Parse a beginning of line character (`^`)
    * @return
    *   [[weaponregex.internal.model.regextree.BOL]] tree node
    * @example
    *   `"^"`
    */
  protected val bol: P[BOL] =
    indexed(P.char('^'))
      .map { case (loc, _) => BOL(loc) }
      .withContext("beginning of line")

  /** Parse a beginning of line character (`$`)
    * @return
    *   [[weaponregex.internal.model.regextree.EOL]] tree node
    * @example
    *   `"$"`
    */
  protected val eol: P[EOL] =
    indexed(P.char('$'))
      .map { case (loc, _) => EOL(loc) }
      .withContext("end of line")

  /** Parse a boundary meta-character character
    * @return
    *   [[weaponregex.internal.model.regextree.BOL]] tree node
    * @example
    *   `"\z"`
    * @see
    *   [[weaponregex.internal.parser.Parser.boundaryMetaChars]]
    */
  protected val boundaryMetaChar: P[Boundary] =
    indexed(backslash *> P.defer(P.charIn(boundaryMetaChars)))
      .map { case (loc, b) => Boundary(b, loc) }
      .withContext("boundary meta-character")

  /** Intermediate parsing rule for boundary tokens which can parse either `bol`, `eol` or `boundaryMetaChar`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  protected val boundary: P[RegexTree] = bol | eol | boundaryMetaChar.backtrack

  /** Intermediate parsing rule for meta-character tokens which can parse either `charOct`, `charHex`, `charUnicode`,
    * `charHexBrace` or `escapeChar`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  protected val metaCharacter: P[RegexTree] = P.defer(
    P.oneOf(
      charOct.backtrack ::
        charHex.backtrack ::
        charUnicode.backtrack ::
        charCodePoint.backtrack ::
        hexEscCharConsumer ::
        escapeChar.backtrack ::
        controlChar.backtrack :: Nil
    )
  )

  /** Parse an escape meta-character
    * @return
    *   [[weaponregex.internal.model.regextree.MetaChar]] tree node
    * @example
    *   `"\n"`
    * @see
    *   [[weaponregex.internal.parser.Parser.escapeChars]]
    */
  protected val escapeChar: P[MetaChar] =
    indexed(backslash *> P.defer(P.charIn(escapeChars)))
      .map { case (loc, c) => MetaChar(c.toString(), loc) }
      .withContext("escape character")

  /** Parse an control meta-character based on caret notation
    * @return
    *   [[weaponregex.internal.model.regextree.ControlChar]] tree node
    * @example
    *   `"\cA"`
    * @see
    *   [[https://en.wikipedia.org/wiki/Caret_notation]]
    */
  protected val controlChar: P[ControlChar] =
    indexed(P.string("\\c") *> alpha)
      .map { case (loc, c) => ControlChar(c, loc) }
      .withContext("control character")

  /** Parse a character with octal value
    * @return
    *   [[weaponregex.internal.model.regextree.MetaChar]] tree node
    * @example
    *   `"\012"`
    */
  protected val charOct: P[MetaChar]

  /** Parse a character with hexadecimal value `\xhh`
    * @return
    *   [[weaponregex.internal.model.regextree.MetaChar]] tree node
    * @example
    *   `"\x01"`
    */
  protected val charHex: P[MetaChar] =
    indexed(P.string("\\x") *> hexdig.repExactlyAs[String](2))
      .map { case (loc, hexDigits) => MetaChar("x" + hexDigits, loc) }
      .withContext("hexadecimal character")

  /** Parse a unicode character `\ uhhhh`
    * @return
    *   [[weaponregex.internal.model.regextree.MetaChar]] tree node
    * @example
    *   `"\ u0020"`
    */
  protected val charUnicode: P[MetaChar] =
    indexed(P.string("\\u") *> hexdig.repExactlyAs[String](4))
      .map { case (loc, hexDigits) => MetaChar("u" + hexDigits, loc) }
      .withContext("unicode character")

  /** Parse a character with a code point `\x{h...h}`, where Character.MIN_CODE_POINT <= 0xh...h <=
    * Character.MAX_CODE_POINT and x is [[weaponregex.internal.parser.Parser#codePointEscChar]]
    * @return
    *   [[weaponregex.internal.model.regextree.MetaChar]] tree node
    * @example
    *   `"\ x{0123}"` or `"\ u{0123}"`
    * @see
    *   [[weaponregex.internal.parser.Parser#codePointEscChar]]
    */
  protected val charCodePoint: P[MetaChar] =
    indexed(P.defer(hexdig.rep.string.between(P.string(s"\\$codePointEscChar{"), P.char('}'))))
      .flatMap { case (loc, hexDigits) =>
        if (java.lang.Character.isValidCodePoint(Integer.parseInt(hexDigits, 16)))
          P.pure(MetaChar(s"$codePointEscChar{$hexDigits}", loc))
        else P.failWith(s"Invalid code point: $hexDigits")
      }
      .withContext("code point character")

  /** Used to consume a hexadecimal escape character `\ x` or `\ u` when all other hex related cases are checked and
    * failed to prevent back tracking.
    */
  protected val hexEscCharConsumer: P[RegexTree] =
    P.stringIn(Set("\\x", "\\u")).void.flatMap(_ => P.fail)

  /** Used to consume an octal escape prefix (e.g. `\ 0`) that starts a valid octal escape but is not followed by valid
    * octal digits, so that it hard-fails instead of back-tracking into a plain quoted character. Flavors that treat
    * such a prefix as valid (or as a literal) leave this as a no-op.
    */
  protected val octEscCharConsumer: P[RegexTree] = P.fail

  /** Parse a character range inside a character class
    * @return
    *   [[weaponregex.internal.model.regextree.Range]] tree node
    * @example
    *   `"a-z"`
    */
  protected val range: P[Range] =
    indexed(P.defer(charClassCharLiteral ~ (P.char('-') *> charClassCharLiteral)))
      .map { case (loc, (from, to)) => Range(from, to, loc) }
      .withContext("character range")

  /** Parse special cases of a character literal in a character class
    * @return
    *   The captured character as a string
    */
  protected val charClassCharLiteralSpecialCases: P[Char] = P.fail

  /** Parse a single literal character that is allowed to be in a character class
    * @return
    *   [[weaponregex.internal.model.regextree.Character]] tree node
    * @example
    *   `"{"`
    * @see
    *   [[weaponregex.internal.parser.Parser.charClassSpecialChars]]
    */
  protected val charClassCharLiteral: P[Character] =
    indexed(P.defer(P.charWhere(c => !charClassSpecialChars.contains(c)) | charClassCharLiteralSpecialCases))
      .map { case (loc, c) => Character(c, loc) }
      .withContext("character literal in character class")

  /** Intermediate parsing rule for character class item tokens which can parse either `charClass`,
    * `preDefinedCharClass`, `metaCharacter`, `range`, `quoteChar`, or `charClassCharLiteral`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  protected val classItem: P[RegexTree] = P.defer(
    P.oneOf(
      range.backtrack ::
        charClassCharLiteral.backtrack ::
        charClass.backtrack ::
        preDefinedCharClass.backtrack ::
        unicodeCharClass.backtrack ::
        metaCharacter.backtrack ::
        hexEscCharConsumer ::
        octEscCharConsumer ::
        quoteChar.backtrack :: Nil
    )
  )

  /** Parse a character class
    * @return
    *   [[weaponregex.internal.model.regextree.CharacterClass]] tree node
    * @example
    *   `"[abc]"`
    */
  protected val charClass: P[CharacterClass] = {
    val items: P0[List[RegexTree]] = P.defer0(
      if (minCharClassItem == 0) classItem.rep0
      else classItem.rep.map(_.toList)
    )
    indexed((P.char('^').? ~ items).with1.between(P.char('['), P.char(']')))
      .map { case (loc, (hat, nodes)) =>
        CharacterClass(nodes, loc, isPositive = hat.isEmpty)
      }
      .withContext("character class")
  }

  /** Parse an any(dot) (`.`) predefined character class
    * @return
    *   [[weaponregex.internal.model.regextree.AnyDot]] tree node
    * @example
    *   `"."`
    */
  protected val anyDot: P[AnyDot] =
    indexed(P.char('.').void)
      .map { case (loc, _) => AnyDot(loc) }
      .withContext("any dot")

  /** Parse a predefined character class
    * @return
    *   [[weaponregex.internal.model.regextree.PredefinedCharClass]] tree node
    * @example
    *   `"\d"`
    * @see
    *   [[weaponregex.internal.parser.Parser.predefCharClassChars]]
    */
  protected val preDefinedCharClass: P[PredefinedCharClass] =
    indexed(backslash *> P.defer(P.charIn(predefCharClassChars)))
      .map { case (loc, c) => PredefinedCharClass(c, loc) }
      .withContext("predefined character class")

  protected val propName: P[String] =
    (alpha.void ~ (alpha.void | digit.void | P.char('_')).rep).string

  /** Parse a unicode character class with lone property
    *
    * @return
    *   [[weaponregex.internal.model.regextree.UnicodeCharClass]] tree node
    * @example
    *   `"\p{Alpha}"`
    * @note
    *   This does not check for the validity of the property inside `\p{}`
    */
  protected val unicodeCharClassLoneProperty: P[UnicodeCharClass] =
    indexed(backslash *> P.charIn('p', 'P') ~ (propName.between(P.char('{'), P.char('}'))))
      .map { case (loc, (p, property)) => UnicodeCharClass(property, loc, p == 'p') }

  /** Parse a unicode character class with property and value
    *
    * @return
    *   [[weaponregex.internal.model.regextree.UnicodeCharClass]] tree node
    * @example
    *   `"\p{Script_Extensions=Latin}"`
    * @note
    *   This does not check for the validity of the property inside `\p{}`
    */
  protected val unicodeCharClassPropertyValue: P[UnicodeCharClass] =
    indexed(backslash *> P.charIn('p', 'P') ~ ((propName <* P.char('=')) ~ propName).between(P.char('{'), P.char('}')))
      .map { case (loc, (p, (property, propValue))) =>
        UnicodeCharClass(property, loc, p == 'p', Some(propValue))
      }

  /** Parse a unicode character class
    *
    * @return
    *   [[weaponregex.internal.model.regextree.UnicodeCharClass]] tree node
    * @example
    *   `"\p{Alpha}"` or `"\p{Script_Extensions=Latin}"`
    * @note
    *   This does not check for the validity of the property inside `\p{}`
    */
  protected val unicodeCharClass: P[UnicodeCharClass] =
    (unicodeCharClassPropertyValue.backtrack | unicodeCharClassLoneProperty)
      .withContext("unicode character class")

  /** Parse the tail part of a long quantifier
    * @return
    *   A tuple of the quantifier minimum and optional maximum part
    */
  protected val quantifierLongTail: P[(Int, Option[Option[Int]])] =
    number ~ (P.char(',') *> number.?).? <* P.char('}')

  /** Parse the quantifier type suffix that may follow a short or long quantifier: `?` for reluctant, `+` for
    * possessive, or nothing for greedy.
    */
  private val quantifierTypeSuffix: P0[QuantifierType] =
    P.charIn('?', '+').?.map {
      case Some('?') => ReluctantQuantifier
      case Some('+') => PossessiveQuantifier
      case _         => GreedyQuantifier
    }

  /** Parse the trailing quantifier (short `?`/`*`/`+` or long `{n}`/`{n,}`/`{n,m}`) that applies to an already-parsed
    * expression.
    * @return
    *   a function producing the quantified [[weaponregex.internal.model.regextree.RegexTree]] node
    */
  protected val quantifierTail: P[(RegexTree, Location) => RegexTree] = {
    val short: P[(RegexTree, Location) => RegexTree] =
      (P.charIn('?', '*', '+') ~ quantifierTypeSuffix)
        .map { case (symbol, qt) =>
          (expr: RegexTree, loc: Location) =>
            (symbol: @unchecked) match {
              case '?' => ZeroOrOne(expr, loc, qt)
              case '*' => ZeroOrMore(expr, loc, qt)
              case '+' => OneOrMore(expr, loc, qt)
            }
        }
        .withContext("short quantifier")

    val long: P[(RegexTree, Location) => RegexTree] =
      ((P.char('{') *> quantifierLongTail) ~ quantifierTypeSuffix)
        .flatMap {
          case ((num, Some(Some(max))), _) if num > max =>
            P.failWith(s"Invalid quantifier: minimum $num is greater than maximum $max")
          case ((num, optionMax), qt) =>
            P.pure { (expr: RegexTree, loc: Location) =>
              optionMax match {
                case None            => Quantifier(expr, num, loc, qt)
                case Some(None)      => Quantifier(expr, num, Quantifier.Infinity, loc, qt)
                case Some(Some(max)) => Quantifier(expr, num, max, loc, qt)
              }
            }
        }
        .backtrack
        .withContext("long quantifier")

    short | long
  }

  /** Parse a capturing group
    * @return
    *   [[weaponregex.internal.model.regextree.Group]] tree node
    * @example
    *   `"(a)"`
    */
  protected val group: P[Group] =
    indexed(P.defer0(RE).with1.between(P.char('('), P.char(')')))
      .map { case (loc, expr) => Group(expr, isCapturing = true, loc) }
      .withContext("capturing group")

  /** Parse a group name
    * @return
    *   the parsed name string
    * @example
    *   `"name1"`
    */
  protected val groupName: P[String]

  /** Parse a named-capturing group
    * @return
    *   [[weaponregex.internal.model.regextree.NamedGroup]] tree node
    * @example
    *   `"(?<name1>hello)"`
    */
  protected val namedGroup: P[NamedGroup] =
    indexed((P.defer(groupName).between(P.char('<'), P.char('>')) ~ P.defer0(RE)).between(P.string("(?"), P.char(')')))
      .map { case (loc, (name, expr)) => NamedGroup(expr, name, loc) }
      .withContext("named capturing group")

  /** Parse a non-capturing group
    * @return
    *   [[weaponregex.internal.model.regextree.Group]] tree node
    * @example
    *   `"(?:hello)"`
    */
  protected val nonCapturingGroup: P[Group] =
    indexed(P.defer0(RE).with1.between(P.string("(?:"), P.char(')')))
      .map { case (loc, expr) => Group(expr, isCapturing = false, loc) }
      .withContext("non-capturing group")

  /** Parse flag literal characters given a string contains all allowed flags
    * @param fs
    *   The string contains all allowed flags
    * @return
    *   [[weaponregex.internal.model.regextree.Flags]] tree node
    * @example
    *   `"idmsuxU"`
    * @note
    *   This is a Scala/Java-only regex syntax
    */
  // `.filter()` function from cats-parse is wrongly mutated by Stryker4s into `.filterNot()` which does not exist in cats-parse
  @SuppressWarnings(Array("stryker4s.mutation.MethodExpression"))
  protected def flags(fs: String): P0[Flags] =
    indexed0(charLiteral.filter(c => fs.contains(c.char)).backtrack.rep0)
      .map { case (loc, fs) => Flags(fs, loc) }

  /** Parse a flag toggle (`idmsuxU-idmsuxU`)
    * @return
    *   [[weaponregex.internal.model.regextree.FlagToggle]] tree node
    * @example
    *   `"idmsuxU-idmsuxU"`
    * @note
    *   This is a Scala/Java-only regex syntax
    */
  protected def flagToggle(fs: String): P0[FlagToggle] =
    indexed0((flags(fs) ~ P.char('-').? ~ flags(fs)).map { case ((on, dash), off) => (on, dash, off) })
      .map { case (loc, (onFlags, dash, offFlags)) => FlagToggle(onFlags, dash.isDefined, offFlags, loc) }

  /** Parse a flag toggle group
    * @return
    *   [[weaponregex.internal.model.regextree.FlagToggleGroup]]
    * @example
    *   `"(?id-mu)"`
    * @note
    *   This is a Scala/Java-only regex syntax
    */
  protected val flagToggleGroup: P[FlagToggleGroup] =
    indexed(flagToggle("idmsuxU").with1.between(P.string("(?"), P.char(')')))
      .map { case (loc, ft) => FlagToggleGroup(ft, loc) }
      .withContext("flag toggle group")

  /** Parse a non-capturing group with flags
    * @return
    *   [[weaponregex.internal.model.regextree.FlagNCGroup]]
    * @example
    *   `"(?id-mu:abc)"`
    * @note
    *   This is a Scala/Java-only regex syntax
    */
  protected val flagNCGroup: P[FlagNCGroup] =
    indexed((flagToggle("idmsux") ~ (P.char(':') *> P.defer0(RE))).with1.between(P.string("(?"), P.char(')')))
      .map { case (loc, (ft, expr)) => FlagNCGroup(ft, expr, loc) }
      .withContext("flag non-capturing group")

  /** Parse a positive or negative lookahead or lookbehind
    * @return
    *   [[weaponregex.internal.model.regextree.Lookaround]]
    * @example
    *   `"(?=abc)"` (positive lookahead)
    */
  protected val lookaround: P[Lookaround] =
    indexed((P.char('<').? ~ P.charIn('=', '!') ~ P.defer0(RE)).with1.between(P.string("(?"), P.char(')')))
      .map { case (loc, ((angleBracket, posNeg), expr)) => Lookaround(expr, posNeg == '=', angleBracket.isEmpty, loc) }
      .withContext("lookaround")

  /** Parse an independent non-capturing group
    *
    * @return
    *   [[weaponregex.internal.model.regextree.AtomicGroup]]
    * @example
    *   `"(?>abc)"`
    * @note
    *   This is a Scala/Java-only regex syntax
    */
  protected val atomicGroup: P[AtomicGroup] =
    indexed(P.defer0(RE).with1.between(P.string("(?>"), P.char(')')))
      .map { case (loc, expr) => AtomicGroup(expr, loc) }
      .withContext("atomic group")

  /** Intermediate parsing rule for special construct tokens which can parse either `namedGroup`, `nonCapturingGroup` or
    * `lookaround`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  protected val specialConstruct: P[RegexTree] =
    P.oneOf(namedGroup.backtrack :: nonCapturingGroup.backtrack :: lookaround.backtrack :: Nil)

  /** Intermediate parsing rule for capturing-related tokens which can parse either `group` or `specialConstruct`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  protected val capturing: P[RegexTree] = P.defer(group.backtrack | specialConstruct)

  /** Parse a reference to a named capturing group
    * @return
    *   [[weaponregex.internal.model.regextree.NameReference]]
    * @example
    *   `"\k<name1>"`
    */
  protected val nameReference: P[NameReference] =
    indexed(P.defer(groupName).between(P.string("\\k<"), P.char('>')))
      .map { case (loc, name) => NameReference(name, loc) }
      .withContext("named reference")

  /** Parse a numbered reference to a capture group
    * @return
    *   [[weaponregex.internal.model.regextree.NumberReference]]
    * @example
    *   `"\13"`
    */
  protected val numReference: P[NumberReference] =
    indexed(backslash *> (Numbers.nonZeroDigit ~ Numbers.digit.rep0).string)
      .map { case (loc, num) => NumberReference(num.toInt, loc) }
      .withContext("numbered reference")

  /** Intermediate parsing rule for reference tokens which can parse either `nameReference` or `numReference`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  protected val reference: P[RegexTree] = nameReference | numReference.backtrack

  /** Parse a quoted character (any character)
    * @return
    *   [[weaponregex.internal.model.regextree.QuoteChar]]
    * @example
    *   `"\$"`
    */
  protected val quoteChar: P[QuoteChar] =
    indexed(backslash *> P.anyChar)
      .map { case (loc, char) => QuoteChar(char, loc) }
      .withContext("quoted character")

  /** Parse a 'long' quote, using `\Q` and `\E`
    * @return
    *   [[weaponregex.internal.model.regextree.Quote]] tree node
    * @example
    *   `"\Qquoted\E"`
    */
  protected val quoteLong: P[Quote] =
    indexed(P.string("\\Q") *> (P.until0(P.string("\\E")) ~ P.string("\\E").?))
      .map { case (loc, (str, end)) => Quote(str, end.isDefined, loc) }
      .withContext("long quote")

  /** Intermediate parsing rule for quoting tokens which can parse either `quoteLong` or `quoteChar`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  protected val quote: P[RegexTree] = quoteLong | quoteChar

  /** Intermediate parsing rule which can parse either `capturing`, `anyDot`, `preDefinedCharClass`, `boundary`,
    * `charClass`, `reference`, `character` or `quote`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  protected val elementaryRE: P[RegexTree] =
    P.defer(
      P.oneOf(
        charLiteral.backtrack ::
          capturing ::
          anyDot ::
          preDefinedCharClass.backtrack ::
          unicodeCharClass.backtrack ::
          boundary ::
          charClass.backtrack ::
          reference ::
          character ::
          hexEscCharConsumer ::
          octEscCharConsumer ::
          quote :: Nil
      )
    )

  /** Intermediate parsing rule which parses an `elementaryRE` optionally followed by a quantifier
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  protected val basicRE: P[RegexTree] = P.defer(
    indexed(elementaryRE ~ quantifierTail.?).map {
      case (_, (expr, None))          => expr
      case (loc, (expr, Some(build))) => build(expr, loc)
    }
  )

  /** Parse an empty string
    *
    * @return
    *   [[weaponregex.internal.model.regextree.Empty]] tree node
    * @example
    *   `""`
    */
  protected val empty: P0[Empty] =
    indexed0(P.unit)
      .map { case (loc, _) => Empty(loc) }

  /** Parse a concatenation of one or more `basicRE`s. A single `basicRE` is returned as-is; two or more are wrapped in
    * a [[weaponregex.internal.model.regextree.Concat]].
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    * @example
    *   `"abc"`
    */

  protected val simpleRE: P[RegexTree] =
    indexed(basicRE.rep(min = 1))
      .map {
        case (_, nodes) if nodes.tail.isEmpty => nodes.head
        case (loc, nodes)                     => Concat(nodes, loc)
      }

  /** The top-level parsing rule: a `|`-separated sequence of `simpleRE` (or `empty`) alternatives.
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] tree
    * @example
    *   any supported regex
    */
  protected val RE: P[RegexTree] = {
    val alternative: P0[RegexTree] = simpleRE | empty

    // Starts with a real `simpleRE`; the `|`-separated tail is optional (no `|` means a bare `simpleRE`).
    val simpleRELed: P[RegexTree] =
      indexed(simpleRE ~ (P.char('|') *> alternative).rep.?).map {
        case (_, (first, None))         => first
        case (loc, (first, Some(rest))) => Or(first :: rest, loc)
      }

    // Starts with an `empty` alternative (pattern begins with `|`)
    val emptyLed: P[Or] =
      indexed(empty.with1 ~ (P.char('|') *> alternative).rep).map { case (loc, (first, rest)) =>
        Or(first :: rest, loc)
      }

    simpleRELed | emptyLed
  }

  /** Parse the given regex pattern
    * @return
    *   A `Right` of parsed [[weaponregex.internal.model.regextree.RegexTree]] if can be parsed, a `Left` with the error
    *   message otherwise
    */
  final def parse(pattern: String): Either[String, RegexTree] =
    RE.parseAll(pattern).left.map(_.show)
}
