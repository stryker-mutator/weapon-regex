package weaponregex.internal.parser

import fastparse.*
import weaponregex.internal.model.regextree.*

import NoWhitespace.*

/** Concrete parser for JVM flavor of regex
  * @param pattern
  *   The regex pattern to be parsed
  * @note
  *   This class constructor is private, instances must be created using the companion
  *   [[weaponregex.internal.parser.Parser]] object
  * @see
  *   [[https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html]]
  */
private[weaponregex] class ParserJVM private[parser] (pattern: String) extends Parser(pattern) {

  /** Regex special characters
    */
  override val specialChars: String = """()[{\.^$|?*+"""

  /** Special characters within a character class
    */
  override val charClassSpecialChars: String = """[]\&"""

  /** Allowed boundary meta-characters
    */
  override val boundaryMetaChars: String = "bBAGzZ"

  /** Allowed escape characters
    */
  override val escapeChars: String = "\\\\tnrfae" // fastparse needs `////` for a single backslash

  /** Allowed predefined character class characters
    */
  override val predefCharClassChars: String = "dDhHsSvVwW"

  /** Minimum number of character class items of a valid character class
    */
  override val minCharClassItem: Int = 1

  /** The escape character used with a code point
    * @example
    *   `\ x{h..h}` or `\ u{h..h}`
    */
  override val codePointEscChar: String = "x"

  /** Parse a character with octal value `\0n`, `\0nn`, `\0mnn` (0 <= m <= 3, 0 <= n <= 7)
    *
    * @return
    *   [[weaponregex.internal.model.regextree.MetaChar]] tree node
    * @example
    *   `"\012"`
    */
  override def charOct[A: P]: P[MetaChar] =
    Indexed("""\0""" ~ (CharIn("0-3") ~ CharIn("0-7").rep(exactly = 2) | CharIn("0-7").rep(min = 1, max = 2)).!)
      .map { case (loc, octDigits) => MetaChar("0" + octDigits, loc) }

  /** Parse special cases of a character literal in a character class
    * @return
    *   The captured character as a string
    */
  override def charClassCharLiteralSpecialCases[A: P]: P[String] = P("&".! ~ !"&")

  /** Parse a character class content without the surround syntactical symbols, i.e. "naked"
    * @return
    *   [[weaponregex.internal.model.regextree.CharacterClassNaked]] tree node
    * @note
    *   This is used only inside the [[weaponregex.internal.parser.ParserJVM.charClassIntersection]]
    */
  def charClassNaked[A: P]: P[CharacterClassNaked] = Indexed(classItem.rep(minCharClassItem))
    .map { case (loc, nodes) => CharacterClassNaked(nodes, loc) }

  /** Parse a character class intersection used inside a character class.
    *
    * @return
    *   [[weaponregex.internal.model.regextree.CharClassIntersection]] tree node
    * @example
    *   `"abc&&[^bc]&&a-z"`
    */
  def charClassIntersection[A: P]: P[CharClassIntersection] =
    Indexed(charClassNaked.rep(2, sep = "&&"))
      .map { case (loc, nodes) => CharClassIntersection(nodes, loc) }

  /** Parse a character class
    * @return
    *   [[weaponregex.internal.model.regextree.CharacterClass]] tree node
    * @example
    *   `"[abc]"`
    */
  override def charClass[A: P]: P[CharacterClass] =
    Indexed(
      "[" ~ "^".!.? ~ ((charClassIntersection.rep(exactly = 1): P[Seq[RegexTree]]) | classItem.rep(
        minCharClassItem
      )) ~ "]"
    )
      .map { case (loc, (hat, nodes)) => CharacterClass(nodes, loc, isPositive = hat.isEmpty) }

  /** Parse a group name
    * @return
    *   the parsed name string
    * @example
    *   `"name1"`
    */
  override def groupName[A: P]: P[String] =
    P(CharIn("a-z", "A-Z") ~ CharIn("a-z", "A-Z", "0-9").rep).!

  /** Intermediate parsing rule for special construct tokens which can parse either `namedGroup`, `nonCapturingGroup`,
    * `flagToggleGroup`, `flagNCGroup`, `lookaround` or `atomicGroup`
    * @return
    *   [[weaponregex.internal.model.regextree.RegexTree]] (sub)tree
    */
  override def specialConstruct[A: P]: P[RegexTree] = P(
    namedGroup | nonCapturingGroup | flagToggleGroup | flagNCGroup | lookaround | atomicGroup
  )
}
