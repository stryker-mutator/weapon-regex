package weaponregex.parser

import fastparse.*
import NoWhitespace.*
import weaponregex.constant.ErrorMessage
import weaponregex.model.*
import weaponregex.model.regextree.*
import weaponregex.extension.StringExtension.StringIndexExtension

import scala.util.{Failure, Success, Try}

/** Companion object for [[weaponregex.parser.Parser]] class that instantiates flavor-specific parsers instances
  */
object Parser {

  /** Apply the parser to parse the given pattern and flags
    * @param pattern
    *   The regex pattern to be parsed
    * @param flags
    *   The regex flags to be used
    * @return
    *   A `Success` of parsed [[weaponregex.model.regextree.RegexTree]] if can be parsed, a `Failure` otherwise
    */
  def apply(pattern: String, flags: Option[String], flavor: ParserFlavor): Try[RegexTree] =
    flavor match {
      case ParserFlavorJVM =>
        if (flags.isDefined) Failure(new IllegalArgumentException(ErrorMessage.jvmWithStringFlags))
        else new ParserJVM(pattern).parse
      case ParserFlavorJS => new ParserJS(pattern, flags).parse
      case _              => Failure(new IllegalArgumentException(ErrorMessage.unsupportedFlavor))
    }

  /** Apply the parser to parse the given pattern
    * @param pattern
    *   The regex pattern to be parsed
    * @return
    *   A `Success` of parsed [[weaponregex.model.regextree.RegexTree]] if can be parsed, a `Failure` otherwise
    */
  def apply(pattern: String, flavor: ParserFlavor = ParserFlavorJVM): Try[RegexTree] = apply(pattern, None, flavor)
}

/** The based abstract parser
  * @param pattern
  *   The regex pattern to be parsed
  * @note
  *   The parsing rules methods inside this class is created based on the defined grammar
  */
abstract class Parser(val pattern: String) {

  /** Regex special characters
    */
  val specialChars: String

  /** Special characters within a character class
    */
  val charClassSpecialChars: String

  /** Allowed boundary meta-characters
    */
  val boundaryMetaChars: String

  /** Allowed escape characters
    */
  val escapeChars: String

  /** Allowed predefined character class characters
    */
  val predefCharClassChars: String

  /** Minimum number of character class items of a valid character class
    */
  val minCharClassItem: Int

  /** The escape character used with a code point
    * @example
    *   `\ x{h..h}` or `\ u{h..h}`
    */
  val codePointEscChar: String

  /** A higher order parser that add [[weaponregex.model.Location]] index information of the parse of the given parser
    * @param p
    *   the parser to be indexed
    * @return
    *   A tuple of the [[weaponregex.model.Location]] of the parse, and the return of the given parser `p`
    */
  def Indexed[A: P, T](p: => P[T]): P[(Location, T)] = P(Index ~ p ~ Index)
    .map { case (i, t, j) => (pattern.locationOf(i, j), t) }

  /** Parse an integer with any number of digits between 0 and 9
    * @return
    *   the parsed integer
    * @example
    *   `"123"`
    */
  def number[A: P]: P[Int] = P(CharIn("0-9").rep(1).!) map (_.toInt)

  /** Parse special cases of a character literal
    * @return
    *   The captured character as a string
    */
  def charLiteralSpecialCases[A: P]: P[String] = Fail

  /** Parse a single literal character that is not a regex special character
    * @return
    *   [[weaponregex.model.regextree.Character]] tree node
    * @example
    *   `"a"`
    * @see
    *   [[weaponregex.parser.Parser.specialChars]]
    */
  def charLiteral[A: P]: P[Character] =
    Indexed(CharPred(!specialChars.contains(_)).! | charLiteralSpecialCases)
      .map { case (loc, c) => Character(c.head, loc) }

  /** Intermediate parsing rule for character-related tokens which can parse either `metaCharacter` or `charLiteral`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def character[A: P]: P[RegexTree] = P(metaCharacter | charLiteral)

  /** Parse a beginning of line character (`^`)
    * @return
    *   [[weaponregex.model.regextree.BOL]] tree node
    * @example
    *   `"^"`
    */
  def bol[A: P]: P[BOL] = Indexed(P("^"))
    .map { case (loc, _) => BOL(loc) }

  /** Parse a beginning of line character (`$`)
    * @return
    *   [[weaponregex.model.regextree.EOL]] tree node
    * @example
    *   `"$"`
    */
  def eol[A: P]: P[EOL] = Indexed(P("$"))
    .map { case (loc, _) => EOL(loc) }

  /** Parse a boundary meta-character character
    * @return
    *   [[weaponregex.model.regextree.BOL]] tree node
    * @example
    *   `"\z"`
    * @see
    *   [[weaponregex.parser.Parser.boundaryMetaChars]]
    */
  def boundaryMetaChar[A: P]: P[Boundary] = Indexed("""\""" ~ CharPred(boundaryMetaChars.contains(_)).!)
    .map { case (loc, b) => Boundary(b, loc) }

  /** Intermediate parsing rule for boundary tokens which can parse either `bol`, `eol` or `boundaryMetaChar`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def boundary[A: P]: P[RegexTree] = P(bol | eol | boundaryMetaChar)

  /** Intermediate parsing rule for meta-character tokens which can parse either `charOct`, `charHex`, `charUnicode`,
    * `charHexBrace` or `escapeChar`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def metaCharacter[A: P]: P[RegexTree] = P(
    charOct | charHex | charUnicode | charCodePoint | hexEscCharConsumer | escapeChar | controlChar
  )

  /** Parse an escape meta-character
    * @return
    *   [[weaponregex.model.regextree.MetaChar]] tree node
    * @example
    *   `"\n"`
    * @see
    *   [[weaponregex.parser.Parser.escapeChars]]
    */
  def escapeChar[A: P]: P[MetaChar] =
    Indexed("""\""" ~ CharPred(escapeChars.contains(_)).!)
      .map { case (loc, c) => MetaChar(c, loc) }

  /** Parse an control meta-character based on caret notation
    * @return
    *   [[weaponregex.model.regextree.ControlChar]] tree node
    * @example
    *   `"\cA"`
    * @see
    *   [[https://en.wikipedia.org/wiki/Caret_notation]]
    */
  def controlChar[A: P]: P[ControlChar] =
    Indexed("""\c""" ~ CharIn("a-zA-Z").!)
      .map { case (loc, c) => ControlChar(c, loc) }

  /** Parse a character with octal value
    * @return
    *   [[weaponregex.model.regextree.MetaChar]] tree node
    * @example
    *   `"\012"`
    */
  def charOct[A: P]: P[MetaChar]

  /** Parse a single hexadecimal digit
    * @return
    *   the parsed hexadecimal digit as a `String`
    * @example
    *   `"F"`
    */
  def hexDigit[A: P]: P[String] = P(CharIn("0-9a-fA-F").!)

  /** Parse a character with hexadecimal value `\xhh`
    * @return
    *   [[weaponregex.model.regextree.MetaChar]] tree node
    * @example
    *   `"\x01"`
    */
  def charHex[A: P]: P[MetaChar] = Indexed("""\x""" ~ hexDigit.rep(exactly = 2).!)
    .map { case (loc, hexDigits) => MetaChar("x" + hexDigits, loc) }

  /** Parse a unicode character `\ uhhhh`
    * @return
    *   [[weaponregex.model.regextree.MetaChar]] tree node
    * @example
    *   `"\ u0020"`
    */
  def charUnicode[A: P]: P[MetaChar] = Indexed("\\u" ~ hexDigit.rep(exactly = 4).!)
    .map { case (loc, hexDigits) => MetaChar("u" + hexDigits, loc) }

  /** Parse a character with a code point `\x{h...h}`, where Character.MIN_CODE_POINT <= 0xh...h <=
    * Character.MAX_CODE_POINT and x is [[weaponregex.parser.Parser.codePointEscChar]]
    * @return
    *   [[weaponregex.model.regextree.MetaChar]] tree node
    * @example
    *   `"\ x{0123}"` or `"\ u{0123}"`
    * @see
    *   [[weaponregex.parser.Parser.codePointEscChar]]
    */
  def charCodePoint[A: P]: P[MetaChar] =
    Indexed(s"\\$codePointEscChar" ~ "{" ~ hexDigit.rep(1).! ~ "}")
      .map {
        case (loc, hexDigits) if java.lang.Character.isValidCodePoint(Integer.parseInt(hexDigits, 16)) =>
          MetaChar(s"$codePointEscChar{$hexDigits}", loc)
        case _ =>
          Fail
          null
      }

  /** Used to consume a hexadecimal escape character `\ x` or `\ u` when all other hex related cases are checked and
    * failed to prevent back tracking.
    * @return
    *   a `null` dummy
    */
  def hexEscCharConsumer[A: P]: P[RegexTree] = {
    P("\\" ~ CharIn("xu"))./
    null
  }

  /** Parse a character range inside a character class
    * @return
    *   [[weaponregex.model.regextree.Range]] tree node
    * @example
    *   `"a-z"`
    */
  def range[A: P]: P[Range] = Indexed(charClassCharLiteral ~ "-" ~ charClassCharLiteral)
    .map { case (loc, (from, to)) => Range(from, to, loc) }

  /** Parse special cases of a character literal in a character class
    * @return
    *   The captured character as a string
    */
  def charClassCharLiteralSpecialCases[A: P]: P[String] = Fail

  /** Parse a single literal character that is allowed to be in a character class
    * @return
    *   [[weaponregex.model.regextree.Character]] tree node
    * @example
    *   `"{"`
    * @see
    *   [[weaponregex.parser.Parser.charClassSpecialChars]]
    */
  def charClassCharLiteral[A: P]: P[Character] =
    Indexed(CharPred(!charClassSpecialChars.contains(_)).! | charClassCharLiteralSpecialCases)
      .map { case (loc, c) => Character(c.head, loc) }

  /** Intermediate parsing rule for character class item tokens which can parse either `charClass`,
    * `preDefinedCharClass`, `metaCharacter`, `range`, `quoteChar`, or `charClassCharLiteral`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def classItem[A: P]: P[RegexTree] = P(
    charClass | preDefinedCharClass | posixCharClass | metaCharacter | range | quoteChar | charClassCharLiteral
  )

  /** Parse a character class
    * @return
    *   [[weaponregex.model.regextree.CharacterClass]] tree node
    * @example
    *   `"[abc]"`
    */
  def charClass[A: P]: P[CharacterClass] = Indexed("[" ~ "^".!.? ~ classItem.rep(minCharClassItem) ~ "]")
    .map { case (loc, (hat, nodes)) => CharacterClass(nodes, loc, isPositive = hat.isEmpty) }

  /** Parse an any(dot) (`.`) predefined character class
    * @return
    *   [[weaponregex.model.regextree.AnyDot]] tree node
    * @example
    *   `"."`
    */
  def anyDot[A: P]: P[AnyDot] = Indexed(P("."))
    .map { case (loc, _) => AnyDot(loc) }

  /** Parse a predefined character class
    * @return
    *   [[weaponregex.model.regextree.PredefinedCharClass]] tree node
    * @example
    *   `"\d"`
    * @see
    *   [[weaponregex.parser.Parser.predefCharClassChars]]
    */
  def preDefinedCharClass[A: P]: P[PredefinedCharClass] =
    Indexed("""\""" ~ CharPred(predefCharClassChars.contains(_)).!)
      .map { case (loc, c) => PredefinedCharClass(c, loc) }

  /** Parse a posix character class
    * @return
    *   [[weaponregex.model.regextree.POSIXCharClass]] tree node
    * @example
    *   `"\p{Alpha}"`
    * @note
    *   This does not check for the validity of the property inside `\p{}`
    */
  def posixCharClass[A: P]: P[POSIXCharClass] =
    Indexed("""\""" ~ CharIn("pP").! ~ "{" ~ (CharIn("a-z", "A-Z") ~ CharIn("a-z", "A-Z", "0-9", "_").rep).! ~ "}")
      .map { case (loc, (p, property)) => POSIXCharClass(property, loc, p == "p") }

  /** A higher order parser that add [[weaponregex.model.regextree.QuantifierType]] information of the parse of the
    * given (quantifier) parser
    * @param p
    *   the quantifier parser
    * @return
    *   A tuple of the return of the given (quantifier) parser `p`, and its
    *   [[weaponregex.model.regextree.QuantifierType]]
    */
  def quantifierType[A: P, T](p: => P[T]): P[(T, QuantifierType)] = P(p ~ CharIn("?+").!.?)
    .map { case (pp, optionQType) =>
      (
        pp,
        optionQType match {
          case Some("?") => ReluctantQuantifier
          case Some("+") => PossessiveQuantifier
          case _         => GreedyQuantifier
        }
      )
    }

  /** Parse a shorthand notation for quantifier (`?`, `*`, `+`)
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    * @example
    *   `"a*"`
    */
  def quantifierShort[A: P]: P[RegexTree] = Indexed(quantifierType(elementaryRE ~ CharIn("?*+").!))
    .map { case (loc, ((expr, q), quantifierType)) =>
      q match {
        case "?" => ZeroOrOne(expr, loc, quantifierType)
        case "*" => ZeroOrMore(expr, loc, quantifierType)
        case "+" => OneOrMore(expr, loc, quantifierType)
      }
    }

  /** Parse the tail part of a long quantifier
    * @return
    *   A tuple of the quantifier minimum and optional maximum part
    */
  def quantifierLongTail[A: P]: P[(Int, Option[Option[Int]])] = number ~ ("," ~ number.?).? ~ "}"

  /** Parse a (full) quantifier (`{n}`, `{n,}`, `{n,m}`)
    * @return
    *   [[weaponregex.model.regextree.Quantifier]] tree node
    * @example
    *   `"a{1}"`
    */
  // `.filter()` function from fastparse is wrongly mutated by Stryker4s into `.filterNot()` which does not exist in fastparse
  @SuppressWarnings(Array("stryker4s.mutation.MethodExpression"))
  def quantifierLong[A: P]: P[Quantifier] =
    Indexed(quantifierType(elementaryRE ~ "{" ~ quantifierLongTail))
      .filter {
        case (_, ((_, (min, Some(Some(max)))), _)) => min <= max
        case _                                     => true
      }
      .map { case (loc, ((expr, (num, optionMax)), quantifierType)) =>
        optionMax match {
          case None            => Quantifier(expr, num, loc, quantifierType)
          case Some(None)      => Quantifier(expr, num, Quantifier.Infinity, loc, quantifierType)
          case Some(Some(max)) => Quantifier(expr, num, max, loc, quantifierType)
        }
      }

  /** Intermediate parsing rule for quantifier tokens which can parse either `quantifierShort` or `quantifierLong`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def quantifier[A: P]: P[RegexTree] = P(quantifierShort | quantifierLong)

  /** Parse a capturing group
    * @return
    *   [[weaponregex.model.regextree.Group]] tree node
    * @example
    *   `"(a)"`
    */
  def group[A: P]: P[Group] = Indexed("(" ~ RE ~ ")")
    .map { case (loc, expr) => Group(expr, isCapturing = true, loc) }

  /** Parse a group name that starts with a letter and followed by zero or more alphanumeric characters
    * @return
    *   the parsed name string
    * @example
    *   `"name1"`
    */
  def groupName[A: P]: P[String] = P(CharIn("a-z", "A-Z") ~ CharIn("a-z", "A-Z", "0-9").rep).!

  /** Parse a named-capturing group
    * @return
    *   [[weaponregex.model.regextree.NamedGroup]] tree node
    * @example
    *   `"(?<name1>hello)"`
    */
  def namedGroup[A: P]: P[NamedGroup] = Indexed("(?<" ~ groupName ~ ">" ~ RE ~ ")")
    .map { case (loc, (name, expr)) => NamedGroup(expr, name, loc) }

  /** Parse a non-capturing group
    * @return
    *   [[weaponregex.model.regextree.Group]] tree node
    * @example
    *   `"(?:hello)"`
    */
  def nonCapturingGroup[A: P]: P[Group] = Indexed("(?:" ~ RE ~ ")")
    .map { case (loc, expr) => Group(expr, isCapturing = false, loc) }

  /** Parse flag literal characters given a string contains all allowed flags
    * @param fs
    *   The string contains all allowed flags
    * @return
    *   [[weaponregex.model.regextree.Flags]] tree node
    * @example
    *   `"idmsuxU"`
    * @note
    *   This is a Scala/Java-only regex syntax
    */
  // `.filter()` function from fastparse is wrongly mutated by Stryker4s into `.filterNot()` which does not exist in fastparse
  @SuppressWarnings(Array("stryker4s.mutation.MethodExpression"))
  def flags[A: P](fs: String): P[Flags] = Indexed(charLiteral.filter(c => fs.contains(c.char)).rep)
    .map { case (loc, fs) => Flags(fs, loc) }

  /** Parse a flag toggle (`idmsuxU-idmsuxU`)
    * @return
    *   [[weaponregex.model.regextree.FlagToggle]] tree node
    * @example
    *   `"idmsuxU-idmsuxU"`
    * @note
    *   This is a Scala/Java-only regex syntax
    */
  def flagToggle[A: P](fs: String): P[FlagToggle] = Indexed(flags(fs) ~ "-".!.? ~ flags(fs))
    .map { case (loc, (onFlags, dash, offFlags)) => FlagToggle(onFlags, dash.isDefined, offFlags, loc) }

  /** Parse a flag toggle group
    * @return
    *   [[weaponregex.model.regextree.FlagToggleGroup]]
    * @example
    *   `"(?id-mu)"`
    * @note
    *   This is a Scala/Java-only regex syntax
    */
  def flagToggleGroup[A: P]: P[FlagToggleGroup] = Indexed("(?" ~ flagToggle("idmsuxU") ~ ")")
    .map { case (loc, ft) => FlagToggleGroup(ft, loc) }

  /** Parse a non-capturing group with flags
    * @return
    *   [[weaponregex.model.regextree.FlagNCGroup]]
    * @example
    *   `"(?id-mu:abc)"`
    * @note
    *   This is a Scala/Java-only regex syntax
    */
  def flagNCGroup[A: P]: P[FlagNCGroup] = Indexed("(?" ~ flagToggle("idmsux") ~ ":" ~ RE ~ ")")
    .map { case (loc, (ft, expr)) => FlagNCGroup(ft, expr, loc) }

  /** Parse a positive or negative lookahead or lookbehind
    * @return
    *   [[weaponregex.model.regextree.Lookaround]]
    * @example
    *   `"(?=abc)"` (positive lookahead)
    */
  def lookaround[A: P]: P[Lookaround] = Indexed("(?" ~ "<".!.? ~ CharIn("=!").! ~ RE ~ ")")
    .map { case (loc, (angleBracket, posNeg, expr)) => Lookaround(expr, posNeg == "=", angleBracket.isEmpty, loc) }

  /** Parse an independent non-capturing group
    *
    * @return
    *   [[weaponregex.model.regextree.AtomicGroup]]
    * @example
    *   `"(?>abc)"`
    * @note
    *   This is a Scala/Java-only regex syntax
    */
  def atomicGroup[A: P]: P[AtomicGroup] = Indexed("(?>" ~ RE ~ ")")
    .map { case (loc, expr) => AtomicGroup(expr, loc) }

  /** Intermediate parsing rule for special construct tokens which can parse either `namedGroup`, `nonCapturingGroup` or
    * `lookaround`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def specialConstruct[A: P]: P[RegexTree] = P(
    namedGroup | nonCapturingGroup | lookaround
  )

  /** Intermediate parsing rule for capturing-related tokens which can parse either `group` or `specialConstruct`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def capturing[A: P]: P[RegexTree] = P(group | specialConstruct)

  /** Parse a reference to a named capturing group
    * @return
    *   [[weaponregex.model.regextree.NameReference]]
    * @example
    *   `"\k<name1>"`
    */
  def nameReference[A: P]: P[NameReference] = Indexed("""\k<""" ~ groupName ~ ">")
    .map { case (loc, name) => NameReference(name, loc) }

  /** Parse a numbered reference to a capture group
    * @return
    *   [[weaponregex.model.regextree.NumberReference]]
    * @example
    *   `"\13"`
    */
  def numReference[A: P]: P[NumberReference] = Indexed("""\""" ~ (CharIn("1-9") ~ CharIn("0-9").rep).!)
    .map { case (loc, num) => NumberReference(num.toInt, loc) }

  /** Intermediate parsing rule for reference tokens which can parse either `nameReference` or `numReference`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def reference[A: P]: P[RegexTree] = P(nameReference | numReference)

  /** Parse a quoted character (any character)
    * @return
    *   [[weaponregex.model.regextree.QuoteChar]]
    * @example
    *   `"\$"`
    */
  def quoteChar[A: P]: P[QuoteChar] = Indexed("""\""" ~ AnyChar.!)
    .map { case (loc, char) => QuoteChar(char.head, loc) }

  /** Parse a 'long' quote, using `\Q` and `\E`
    * @return
    *   [[weaponregex.model.regextree.Quote]] tree node
    * @example
    *   `"\Qquoted\E"`
    */
  def quoteLong[A: P]: P[Quote] = Indexed("""\Q""" ~ (!"""\E""" ~ AnyChar).rep.! ~ """\E""".!.?)
    .map { case (loc, (str, end)) => Quote(str, end.isDefined, loc) }

  /** Intermediate parsing rule for quoting tokens which can parse either `quoteLong` or `quoteChar`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def quote[A: P]: P[RegexTree] = P(quoteLong | quoteChar)

  /** Intermediate parsing rule which can parse either `capturing`, `anyDot`, `preDefinedCharClass`, `boundary`,
    * `charClass`, `reference`, `character` or `quote`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def elementaryRE[A: P]: P[RegexTree] = P(
    capturing | anyDot | preDefinedCharClass | posixCharClass | boundary | charClass | reference | character | quote
  )

  /** Intermediate parsing rule which can parse either `quantifier` or `elementaryRE`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def basicRE[A: P]: P[RegexTree] = P(quantifier | elementaryRE)

  /** Parse a concatenation of `basicRE`s
    * @return
    *   [[weaponregex.model.regextree.Concat]] tree node
    * @example
    *   `"abc"`
    */
  def concat[A: P]: P[Concat] = Indexed(basicRE.rep(2))
    .map { case (loc, nodes) => Concat(nodes, loc) }

  /** Parse an empty string
    *
    * @return
    *   [[weaponregex.model.regextree.Empty]] tree node
    * @example
    *   `""`
    */
  def empty[A: P]: P[Empty] = Indexed("")
    .map { case (loc, _) => Empty(loc) }

  /** Intermediate parsing rule which can parse either `concat`, `basicRE` or `empty`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def simpleRE[A: P]: P[RegexTree] = P(concat | basicRE | empty)

  /** Parse an 'or' (`|`) of `simpleRE`
    * @return
    *   [[weaponregex.model.regextree.Or]] tree node
    * @example
    *   `"a|b|c"`
    */
  def or[A: P]: P[Or] = Indexed(simpleRE.rep(2, sep = "|"))
    .map { case (loc, nodes) => Or(nodes, loc) }

  /** The top-level parsing rule which can parse either `or` or `simpleRE`
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] tree
    * @example
    *   any supported regex
    */
  def RE[A: P]: P[RegexTree] = P(or | simpleRE)

  /** The entry point of the parser that should parse from the start to the end of the regex string
    * @return
    *   [[weaponregex.model.regextree.RegexTree]] tree
    */
  def entry[A: P]: P[RegexTree] = P(Start ~ RE ~ End)

  /** Parse the given regex pattern
    * @return
    *   A `Success` of parsed [[weaponregex.model.regextree.RegexTree]] if can be parsed, a `Failure` otherwise
    */
  def parse: Try[RegexTree] = fastparse.parse(pattern, entry(_)) match {
    case Parsed.Success(regexTree: RegexTree, _) => Success(regexTree)
    case f: Parsed.Failure => Failure(new RuntimeException(ErrorMessage.parserErrorHeader + f.msg))
  }
}
