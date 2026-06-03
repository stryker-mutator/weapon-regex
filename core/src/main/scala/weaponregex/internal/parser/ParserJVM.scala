package weaponregex.internal.parser

import cats.parse.Rfc5234.*
import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import weaponregex.internal.model.regextree.*

/** Parser instance for JVM flavor of regex
  * @note
  *   This object is private, instances must be accessed [[weaponregex.internal.parser.Parser]] object
  * @see
  *   [[https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html]]
  */
private[weaponregex] object ParserJVM extends Parser {

  /** Regex special characters
    */
  override protected val specialChars: String = """()[{\.^$|?*+"""

  /** Special characters within a character class
    */
  override protected val charClassSpecialChars: String = """[]\&"""

  /** Allowed boundary meta-characters
    */
  override protected val boundaryMetaChars: String = "bBAGzZ"

  /** Allowed escape characters
    */
  override protected val escapeChars: String = "\\\\tnrfae" // fastparse needs `////` for a single backslash

  /** Allowed predefined character class characters
    */
  override protected val predefCharClassChars: String = "dDhHsSvVwW"

  /** Minimum number of character class items of a valid character class
    */
  override protected val minCharClassItem: Int = 1

  /** The escape character used with a code point
    * @example
    *   `\ x{h..h}` or `\ u{h..h}`
    */
  override protected val codePointEscChar: Char = 'x'

  /** Parse a character with octal value `\0n`, `\0nn`, `\0mnn` (0 <= m <= 3, 0 <= n <= 7)
    *
    * @return
    *   [[weaponregex.internal.model.regextree.MetaChar]] tree node
    * @example
    *   `"\012"`
    */
  override protected val charOct: P[MetaChar] =
    indexed(
      P.string("\\0") *> ((P.charIn('0' to '3') ~ P
        .charIn('0' to '7')
        .repExactlyAs[String](2)).string.backtrack | P.charIn('0' to '7').rep(1, 2).string)
    )
      .map { case (loc, octDigits) => MetaChar("0" + octDigits, loc) }
      .withContext("octal character")

  /** Parse special cases of a character literal in a character class
    * @return
    *   The captured character as a string
    */
  override protected val charClassCharLiteralSpecialCases: P[Char] =
    (P.char('&').as('&') <* P.not(P.char('&'))).backtrack

  /** Parse a character class content without the surround syntactical symbols, i.e. "naked"
    * @return
    *   [[weaponregex.internal.model.regextree.CharacterClassNaked]] tree node
    * @note
    *   This is used only inside the [[weaponregex.internal.parser.ParserJVM.charClassIntersection]]
    */
  private val charClassNaked: P[CharacterClassNaked] =
    indexed(classItem.rep.map(_.toList))
      .map { case (loc, nodes) => CharacterClassNaked(nodes, loc) }

  /** Parse a character class intersection used inside a character class.
    *
    * @return
    *   [[weaponregex.internal.model.regextree.CharClassIntersection]] tree node
    * @example
    *   `"abc&&[^bc]&&a-z"`
    */
  private val charClassIntersection: P[CharClassIntersection] =
    indexed(charClassNaked.repSep(2, P.string("&&")).map(_.toList))
      .map { case (loc, nodes) => CharClassIntersection(nodes, loc) }

  /** Parse a character class
    * @return
    *   [[weaponregex.internal.model.regextree.CharacterClass]] tree node
    * @example
    *   `"[abc]"`
    */
  override protected val charClass: P[CharacterClass] = {
    val content: P0[List[RegexTree]] = charClassIntersection.backtrack.map(ci => List(ci)) | classItem.rep.map(_.toList)
    indexed((P.char('^').? ~ content).with1.between(P.char('['), P.char(']')))
      .map { case (loc, (hat, nodes)) => CharacterClass(nodes, loc, isPositive = hat.isEmpty) }
      .withContext("character class")
  }

  /** Parse a group name
    * @return
    *   the parsed name string
    * @example
    *   `"name1"`
    */
  override protected val groupName: P[String] =
    (alpha ~
      (alpha | Numbers.digit).rep0).string

  /** Intermediate parsing rule for special construct tokens which can parse either `namedGroup`, `nonCapturingGroup`,
    * `flagToggleGroup`, `flagNCGroup`, `lookaround` or `atomicGroup`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  override protected val specialConstruct: P[RegexTree] =
    P.oneOf(
      namedGroup.backtrack ::
        nonCapturingGroup.backtrack ::
        flagToggleGroup.backtrack ::
        flagNCGroup.backtrack ::
        lookaround.backtrack ::
        atomicGroup.backtrack :: Nil
    )
}
