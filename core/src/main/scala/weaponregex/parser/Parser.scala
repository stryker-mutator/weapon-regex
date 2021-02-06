package weaponregex.parser

import fastparse._
import NoWhitespace._
import weaponregex.model._
import weaponregex.model.regextree._
import weaponregex.extension.StringExtension.StringIndexExtension

import scala.util.{Failure, Success, Try}

/** Companion object for [[weaponregex.parser.Parser]] class that instantiates [[weaponregex.parser.Parser]] instances
  */
object Parser {

  sealed trait ParserFlavor
  case object ParserFlavorJVM extends ParserFlavor
  case object ParserFlavorJS extends ParserFlavor

  /** Apply the parser to parse the given pattern
    * @param pattern The regex pattern to be parsed
    * @return A `Success` of parsed [[weaponregex.model.regextree.RegexTree]] if can be parsed, a `Failure` otherwise
    */
  def apply(pattern: String, flavor: ParserFlavor = ParserFlavorJVM): Try[RegexTree] = flavor match {
    case ParserFlavorJVM => new ParserJVM(pattern).parse
    case ParserFlavorJS  => new ParserJS(pattern).parse
    case _               => Failure(new RuntimeException("[Error] Parser: Unsupported regex flavor"))
  }
}

/** @param pattern The regex pattern to be parsed
  * @note This class constructor is private, instances must be created using the companion [[weaponregex.parser.Parser]] object
  * @note The parsing rules methods inside this class is created based on the defined grammar
  */
abstract class Parser(val pattern: String) {

  /** Regex special characters
    */
  val specialChars: String

  /** A higher order parser that add [[weaponregex.model.Location]] index information of the parse of the given parser
    * @param p the parser to be indexed
    * @return A tuple of the [[weaponregex.model.Location]] of the parse, and the return of the given parser `p`
    */
  def Indexed[_: P, T](p: => P[T]): P[(Location, T)] = P(Index ~ p ~ Index)
    .map { case (i, t, j) => (pattern.locationOf(i, j), t) }

  /** Parse an integer with any number of digits between 0 and 9
    * @return the parsed integer
    * @example `"123"`
    */
  def number[_: P]: P[Int] = P(CharIn("0-9").rep(1).!) map (_.toInt)

  /** Parse a single literal character that is not a regex special character
    * @return [[weaponregex.model.regextree.Character]] tree node
    * @example `"a"`
    */
  def charLiteral[_: P]: P[Character] = Indexed(CharPred(!specialChars.contains(_)).!)
    .map { case (loc, c) => Character(c.head, loc) }

  /** Intermediate parsing rule for character-related tokens which can parse either `metaCharacter` or `charLiteral`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def character[_: P]: P[RegexTree] = P(metaCharacter | charLiteral)

  /** Parse a beginning of line character (`^`)
    * @return [[weaponregex.model.regextree.BOL]] tree node
    * @example `"^"`
    */
  def bol[_: P]: P[BOL] = Indexed(P("^"))
    .map { case (loc, _) => BOL(loc) }

  /** Parse a beginning of line character (`$`)
    * @return [[weaponregex.model.regextree.EOL]] tree node
    * @example `"$"`
    */
  def eol[_: P]: P[EOL] = Indexed(P("$"))
    .map { case (loc, _) => EOL(loc) }

  /** Parse a boundary meta-character character (`\b`, `\B`, `\A`, `\G`, `\z`, `\Z`)
    * @return [[weaponregex.model.regextree.BOL]] tree node
    * @example `"\z"`
    */
  def boundaryMetaChar[_: P]: P[Boundary] = Indexed("""\""" ~ CharIn("bBAGzZ").!)
    .map { case (loc, b) => Boundary(b, loc) }

  /** Intermediate parsing rule for boundary tokens which can parse either `bol`, `eol` or `boundaryMetaChar`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def boundary[_: P]: P[RegexTree] = P(bol | eol | boundaryMetaChar)

  /** Intermediate parsing rule for meta-character tokens which can parse either `charOct`, `charHex`, `charUnicode`, `charHexBrace` or `escapeChar`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def metaCharacter[_: P]: P[RegexTree] = P(charOct | charHex | charUnicode | charHexBrace | escapeChar)

  /** Parse an escape meta-character (`\\`, `\t`, `\n`, `\r`, `\f`)
    * @return [[weaponregex.model.regextree.MetaChar]] tree node
    * @example `"\n"`
    * @note `\a`, `\c`, and `\e` is not supported. `\a` and `\e` are valid but interpreted differently in Scala/Java and JavaScript.
    */
  def escapeChar[_: P]: P[MetaChar] =
    Indexed("""\""" ~ CharIn("\\\\tnrf").!) // fastparse needs //// for a single backslash
      .map { case (loc, c) => MetaChar(c, loc) }

  /** Parse a character with octal value `\0n`, `\0nn`, `\0mn` (0 <= m <= 3, 0 <= n <= 7)
    * @return [[weaponregex.model.regextree.MetaChar]] tree node
    * @example `"\0123"`
    */
  def charOct[_: P]: P[MetaChar] = Indexed("""\0""" ~ CharIn("0-7").!.rep(min = 1, max = 3))
    .map { case (loc, octDigits) => MetaChar("0" + octDigits.mkString, loc) }

  /** Parse a character with hexadecimal value `\xhh`
    * @return [[weaponregex.model.regextree.MetaChar]] tree node
    * @example `"\x01"`
    */
  def charHex[_: P]: P[MetaChar] = Indexed("""\x""" ~ CharIn("0-9a-zA-Z").!.rep(exactly = 2))
    .map { case (loc, hexDigits) => MetaChar("x" + hexDigits.mkString, loc) }

  /** Parse a unicode character `\ uhhhh`
    * @return [[weaponregex.model.regextree.MetaChar]] tree node
    * @example `"\ u0020"`
    */
  def charUnicode[_: P]: P[MetaChar] = Indexed("\\u" ~ CharIn("0-9a-zA-Z").!.rep(exactly = 4))
    .map { case (loc, hexDigits) => MetaChar("u" + hexDigits.mkString, loc) }

  /** Parse a character with hexadecimal value with braces `\x{h...h}` (Character.MIN_CODE_POINT <= 0xh...h <= Character.MAX_CODE_POINT)
    * @return [[weaponregex.model.regextree.MetaChar]] tree node
    * @example `"\x{0123}"`
    */
  def charHexBrace[_: P]: P[MetaChar] = Indexed("""\x{""" ~ CharIn("0-9a-zA-Z").!.rep(1) ~ "}")
    .map { case (loc, hexDigits) => MetaChar("x{" + hexDigits.mkString + "}", loc) }

  /** Parse a character range inside a character class
    * @return [[weaponregex.model.regextree.Range]] tree node
    * @example `"a-z"`
    */
  def range[_: P]: P[Range] = Indexed(charClassCharLiteral ~ "-" ~ charClassCharLiteral)
    .map { case (loc, (from, to)) => Range(from, to, loc) }

  /** Parse a single literal character that is allowed to be in a character class
    * @return [[weaponregex.model.regextree.Character]] tree node
    * @example `"{"`
    * @note The only characters which cannot be in a character class on their own are `[`, `]` and `\`
    */
  def charClassCharLiteral[_: P]: P[Character] = Indexed(CharPred(!"""[]\""".contains(_)).!)
    .map { case (loc, c) => Character(c.head, loc) }

  /** Intermediate parsing rule for character class item tokens which can parse either `charClass`, `preDefinedCharClass`, `metaCharacter`, `range`, `quoteChar`, or `charClassCharLiteral`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    * @note Nested character class is a Scala/Java-only regex syntax
    */
  def classItem[_: P]: P[RegexTree] = P(
    charClass | preDefinedCharClass | metaCharacter | range | quoteChar | charClassCharLiteral
  )

  /** Parse a character class
    * @return [[weaponregex.model.regextree.CharacterClass]] tree node
    * @example `"[abc]"`
    */
  def charClass[_: P]: P[CharacterClass] = Indexed("[" ~ "^".!.? ~ classItem.rep(1) ~ "]")
    .map { case (loc, (hat, nodes)) => CharacterClass(nodes, loc, isPositive = hat.isEmpty) }

  /** Parse an any(dot) (`.`) predefined character class
    * @return [[weaponregex.model.regextree.AnyDot]] tree node
    * @example `"."`
    */
  def anyDot[_: P]: P[AnyDot] = Indexed(P("."))
    .map { case (loc, _) => AnyDot(loc) }

  /** Parse a predefined character class (`\d`, `\D`, `\s`, `\S`, `\w`, `\W`)
    * @return [[weaponregex.model.regextree.PredefinedCharClass]] tree node
    * @example `"\d"`
    */
  def preDefinedCharClass[_: P]: P[PredefinedCharClass] = Indexed("""\""" ~ CharIn("dDsSwW").!)
    .map { case (loc, c) => PredefinedCharClass(c, loc) }

  /** A higher order parser that add [[weaponregex.model.regextree.QuantifierType]] information of the parse of the given (quantifier) parser
    * @param p the quantifier parser
    * @return A tuple of the return of the given (quantifier) parser `p`, and its [[weaponregex.model.regextree.QuantifierType]]
    */
  def quantifierType[_: P, T](p: => P[T]): P[(T, QuantifierType)] = P(p ~ CharIn("?+").!.?)
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
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    * @example `"a*"`
    */
  def quantifierShort[_: P]: P[RegexTree] = Indexed(quantifierType(elementaryRE ~ CharIn("?*+").!))
    .map { case (loc, ((expr, q), quantifierType)) =>
      q match {
        case "?" => ZeroOrOne(expr, loc, quantifierType)
        case "*" => ZeroOrMore(expr, loc, quantifierType)
        case "+" => OneOrMore(expr, loc, quantifierType)
      }
    }

  /** Parse a (full) quantifier (`{n}`, `{n,}`, `{n,m}`)
    * @return [[weaponregex.model.regextree.Quantifier]] tree node
    * @example `"a{1}"`
    */
  def quantifierLong[_: P]: P[Quantifier] =
    Indexed(quantifierType(elementaryRE ~ "{" ~ number ~ ("," ~ number.?).? ~ "}"))
      .map { case (loc, ((expr, num, optionMax), quantifierType)) =>
        optionMax match {
          case None            => Quantifier(expr, num, loc, quantifierType)
          case Some(None)      => Quantifier(expr, num, Quantifier.Infinity, loc, quantifierType)
          case Some(Some(max)) => Quantifier(expr, num, max, loc, quantifierType)
        }
      }

  /** Intermediate parsing rule for quantifier tokens which can parse either `quantifierShort` or `quantifierLong`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def quantifier[_: P]: P[RegexTree] = P(quantifierShort | quantifierLong)

  /** Parse a capturing group
    * @return [[weaponregex.model.regextree.Group]] tree node
    * @example `"(a)"`
    */
  def group[_: P]: P[Group] = Indexed("(" ~ RE ~ ")")
    .map { case (loc, expr) => Group(expr, isCapturing = true, loc) }

  /** Parse a group name that starts with a letter and followed by zero or more alphanumeric characters
    * @return the parsed name string
    * @example `"name1"`
    */
  def groupName[_: P]: P[String] = P(CharIn("a-z", "A-Z") ~ CharIn("a-z", "A-Z", "0-9").rep).!

  /** Parse a named-capturing group
    * @return [[weaponregex.model.regextree.NamedGroup]] tree node
    * @example `"(?<name1>hello)"`
    */
  def namedGroup[_: P]: P[NamedGroup] = Indexed("(?<" ~ groupName ~ ">" ~ RE ~ ")")
    .map { case (loc, (name, expr)) => NamedGroup(expr, name, loc) }

  /** Parse a non-capturing group
    * @return [[weaponregex.model.regextree.Group]] tree node
    * @example `"(?:hello)"`
    */
  def nonCapturingGroup[_: P]: P[Group] = Indexed("(?:" ~ RE ~ ")")
    .map { case (loc, expr) => Group(expr, isCapturing = false, loc) }

  /** Parse flag literal characters given a string contains all allowed flags
    * @param fs The string contains all allowed flags
    * @return [[weaponregex.model.regextree.Flags]] tree node
    * @example `"idmsuxU"`
    * @note This is a Scala/Java-only regex syntax
    */
  // `.filter()` function from fastparse is wrongly mutated by Stryker4s into `.filterNot()` which does not exist in fastparse
  @SuppressWarnings(Array("stryker4s.mutation.MethodExpression"))
  def flags[_: P](fs: String): P[Flags] = Indexed(charLiteral.filter(c => fs.contains(c.char)).rep)
    .map { case (loc, fs) => Flags(fs, loc) }

  /** Parse a flag toggle (`idmsuxU-idmsuxU`)
    * @return [[weaponregex.model.regextree.FlagToggle]] tree node
    * @example `"idmsuxU-idmsuxU"`
    * @note This is a Scala/Java-only regex syntax
    */
  def flagToggle[_: P](fs: String): P[FlagToggle] = Indexed(flags(fs) ~ "-".!.? ~ flags(fs))
    .map { case (loc, (onFlags, dash, offFlags)) => FlagToggle(onFlags, dash.isDefined, offFlags, loc) }

  /** Parse a flag toggle group
    * @return [[weaponregex.model.regextree.FlagToggleGroup]]
    * @example `"(?id-mu)"`
    * @note This is a Scala/Java-only regex syntax
    */
  def flagToggleGroup[_: P]: P[FlagToggleGroup] = Indexed("(?" ~ flagToggle("idmsuxU") ~ ")")
    .map { case (loc, ft) => FlagToggleGroup(ft, loc) }

  /** Parse a non-capturing group with flags
    * @return [[weaponregex.model.regextree.FlagNCGroup]]
    * @example `"(?id-mu:abc)"`
    * @note This is a Scala/Java-only regex syntax
    */
  def flagNCGroup[_: P]: P[FlagNCGroup] = Indexed("(?" ~ flagToggle("idmsux") ~ ":" ~ RE ~ ")")
    .map { case (loc, (ft, expr)) => FlagNCGroup(ft, expr, loc) }

  /** Parse a positive or negative lookahead or lookbehind
    * @return [[weaponregex.model.regextree.Lookaround]]
    * @example `"(?=abc)"` (positive lookahead)
    */
  def lookaround[_: P]: P[Lookaround] = Indexed("(?" ~ "<".!.? ~ CharIn("=!").! ~ RE ~ ")")
    .map { case (loc, (angleBracket, posNeg, expr)) => Lookaround(expr, posNeg == "=", angleBracket.isEmpty, loc) }

  /** Parse an independent non-capturing group
    * @return [[weaponregex.model.regextree.INCGroup]]
    * @example `"(?>abc)"`
    * @note This is a Scala/Java-only regex syntax
    */
  def incGroup[_: P]: P[INCGroup] = Indexed("(?>" ~ RE ~ ")")
    .map { case (loc, expr) => INCGroup(expr, loc) }

  /** Intermediate parsing rule for special construct tokens which can parse either `namedGroup`, `nonCapturingGroup`, `flagToggleGroup`, `flagNCGroup`, `lookaround` or `incGroup`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def specialConstruct[_: P]: P[RegexTree] = P(
    namedGroup | nonCapturingGroup | flagToggleGroup | flagNCGroup | lookaround | incGroup
  )

  /** Intermediate parsing rule for capturing-related tokens which can parse either `group` or `specialConstruct`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def capturing[_: P]: P[RegexTree] = P(group | specialConstruct)

  /** Parse a reference to a named capturing group
    * @return [[weaponregex.model.regextree.NameReference]]
    * @example `"\k<name1>"`
    */
  def nameReference[_: P]: P[NameReference] = Indexed("""\k<""" ~ groupName ~ ">")
    .map { case (loc, name) => NameReference(name, loc) }

  /** Parse a numbered reference to a capture group
    * @return [[weaponregex.model.regextree.NumberReference]]
    * @example `"\13"`
    */
  def numReference[_: P]: P[NumberReference] = Indexed("""\""" ~ (CharIn("1-9") ~ CharIn("0-9").rep).!)
    .map { case (loc, num) => NumberReference(num.toInt, loc) }

  /** Intermediate parsing rule for reference tokens which can parse either `nameReference` or `numReference`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def reference[_: P]: P[RegexTree] = P(nameReference | numReference)

  /** Parse a quoted character (any character)
    * @return [[weaponregex.model.regextree.QuoteChar]]
    * @example `"\$"`
    */
  def quoteChar[_: P]: P[QuoteChar] = Indexed("""\""" ~ AnyChar.!)
    .map { case (loc, char) => QuoteChar(char.head, loc) }

  /** Parse a 'long' quote, using `\Q` and `\E`
    * @return [[weaponregex.model.regextree.Quote]] tree node
    * @example `"\Qquoted\E"`
    */
  def quoteLong[_: P]: P[Quote] = Indexed("""\Q""" ~ (!"""\E""" ~ AnyChar).rep.! ~ """\E""".!.?)
    .map { case (loc, (str, end)) => Quote(str, end.isDefined, loc) }

  /** Intermediate parsing rule for quoting tokens which can parse either `quoteLong` or `quoteChar`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def quote[_: P]: P[RegexTree] = P(quoteLong | quoteChar)

  /** Intermediate parsing rule which can parse either `capturing`, `anyDot`, `preDefinedCharClass`, `boundary`, `charClass`, `reference`, `character` or `quote`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def elementaryRE[_: P]: P[RegexTree] = P(
    capturing | anyDot | preDefinedCharClass | boundary | charClass | reference | character | quote
  )

  /** Intermediate parsing rule which can parse either `quantifier` or `elementaryRE`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def basicRE[_: P]: P[RegexTree] = P(quantifier | elementaryRE)

  /** Parse a concatenation of `basicRE`s
    * @return [[weaponregex.model.regextree.Concat]] tree node
    * @example `"abc"`
    */
  def concat[_: P]: P[Concat] = Indexed(basicRE.rep(2))
    .map { case (loc, nodes) => Concat(nodes, loc) }

  /** Intermediate parsing rule which can parse either `concat` or `basicRE`
    * @return [[weaponregex.model.regextree.RegexTree]] (sub)tree
    */
  def simpleRE[_: P]: P[RegexTree] = P(concat | basicRE)

  /** Parse an 'or' (`|`) of `simpleRE`
    * @return [[weaponregex.model.regextree.Or]] tree node
    * @example `"a|b|c"`
    */
  def or[_: P]: P[Or] = Indexed(simpleRE.rep(2, sep = "|"))
    .map { case (loc, nodes) => Or(nodes, loc) }

  /** The top-level parsing rule which can parse either `or` or `simpleRE`
    * @return [[weaponregex.model.regextree.RegexTree]] tree
    * @example any supported regex
    */
  def RE[_: P]: P[RegexTree] = P(or | simpleRE)

  /** The entry point of the parser that should parse from the start to the end of the regex string
    * @return [[weaponregex.model.regextree.RegexTree]] tree
    */
  def entry[_: P]: P[RegexTree] = P(Start ~ RE ~ End)

  /** Parse the given regex pattern
    * @return A `Success` of parsed [[weaponregex.model.regextree.RegexTree]] if can be parsed, a `Failure` otherwise
    */
  def parse: Try[RegexTree] = fastparse.parse(pattern, entry(_)) match {
    case Parsed.Success(regexTree: RegexTree, index) => Success(regexTree)
    case f @ Parsed.Failure(str, index, extra)       => Failure(new RuntimeException("[Error] Parser: " + f.msg))
  }
}
