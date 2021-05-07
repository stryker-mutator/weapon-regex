package weaponregex.model.regextree

import weaponregex.model.Location

/** The non-terminal node of the [[weaponregex.model.regextree.RegexTree]] that have at least one child node
  * @param children The children that fall under this node
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  * @param prefix The string that is put in front of the node's children when building
  * @param postfix The string that is put after the node's children when building
  * @param sep The string that is put in between the node's children when building
  */
abstract class Node(
    override val children: Seq[RegexTree],
    override val location: Location,
    override val prefix: String = "",
    override val postfix: String = "",
    val sep: String = ""
) extends RegexTree {

  /** Build the node into a String with a child replaced by a string.
    * @param child Child to be replaced
    * @param childString Replacement String
    * @return A String representation of the tree
    */
  override def buildWith(child: RegexTree, childString: String): String =
    children.map(c => if (c eq child) childString else c.build).mkString(prefix, sep, postfix)

  /** Build the node into a String while a predicate holds for a given child.
    * @param pred Predicate on a child
    * @return A String representation of the tree
    */
  override def buildWhile(pred: RegexTree => Boolean): String =
    children.filter(pred).map(_.build).mkString(prefix, sep, postfix)
}

/** Character class node
  * @param nodes The child nodes contained in the character class
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  * @param isPositive `true` if the character class is positive, `false` otherwise
  */
case class CharacterClass(nodes: Seq[RegexTree], override val location: Location, isPositive: Boolean = true)
    extends Node(nodes, location, if (isPositive) "[" else "[^", "]")

/** Character class node without the surround syntactical symbols, i.e. "naked"
  * @param nodes The child nodes contained in the character class
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class CharacterClassNaked(nodes: Seq[RegexTree], override val location: Location) extends Node(nodes, location)

/** Character class intersection used inside a character class
  * @param nodes The nodes that are being "or-ed"
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class CharClassIntersection(nodes: Seq[RegexTree], override val location: Location)
    extends Node(nodes, location, sep = "&&")

/** Character range that is used inside of a character class
  * @param from The left bound of the range
  * @param to The right bound of the range
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Range(from: Character, to: Character, override val location: Location)
    extends Node(Seq(from, to), location, sep = "-")

/** (Non-)capturing group node
  * @param expr The regex inside the group
  * @param isCapturing `true` if group is capturing, `false` otherwise
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Group(
    expr: RegexTree,
    isCapturing: Boolean,
    override val location: Location
) extends Node(Seq(expr), location, if (isCapturing) "(" else "(?:", ")")

/** Named capturing group node
  * @param expr The regex inside the group
  * @param name The name of the group
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class NamedGroup(expr: RegexTree, name: String, override val location: Location)
    extends Node(Seq(expr), location, s"(?<$name>", ")")

/** Non-capturing group with flags
  * @param flagToggle The [[weaponregex.model.regextree.FlagToggle]] object associated with the group
  * @param expr The regex inside the group
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class FlagNCGroup(
    flagToggle: FlagToggle,
    expr: RegexTree,
    override val location: Location
) extends Node(Seq(flagToggle, expr), location, "(?", ")", ":")

/** Flag toggle group node
  * @param flagToggle The [[weaponregex.model.regextree.FlagToggle]] object associated with the group
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class FlagToggleGroup(flagToggle: FlagToggle, override val location: Location)
    extends Node(Seq(flagToggle), location, "(?", ")")

/** Flag toggle node that describes which flags are toggled on and/or off
  * @param onFlags The flags that are toggled on
  * @param hasDash `true` if there is a dash character `-` between the `onFlags` and `offFlags`, `false` otherwise
  * @param offFlags The flags that are toggled off
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class FlagToggle(onFlags: Flags, hasDash: Boolean, offFlags: Flags, override val location: Location)
    extends Node(Seq(onFlags, offFlags), location) {
  override lazy val build: String = onFlags.build + (if (hasDash) "-" else "") + offFlags.build
}

/** A sequence of flags for use in [[weaponregex.model.regextree.FlagToggle]]
  * @param flags The sequence of flag [[weaponregex.model.regextree.Character]]s
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Flags(flags: Seq[Character], override val location: Location) extends Node(flags, location)

/** The umbrella node for positive/negative lookahead/lookbehind
  * @param expr The regex inside the lookaround
  * @param isPositive `true` if the lookaround is positive, `false` otherwise
  * @param isLookahead `true` if this is a lookahead, `false` if this is a lookbehind
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Lookaround(expr: RegexTree, isPositive: Boolean, isLookahead: Boolean, override val location: Location)
    extends Node(
      Seq(expr),
      location,
      "(?"
        + (if (isLookahead) "" else "<")
        + (if (isPositive) "=" else "!"),
      ")"
    )

/** Atomic (independent, non-capturing) group node
  * @param expr The regex inside the group
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class AtomicGroup(expr: RegexTree, override val location: Location) extends Node(Seq(expr), location, "(?>", ")")

/** The enumeration of the quantifier type
  * @param syntax The syntax used to represent the quantifier type
  */
sealed abstract class QuantifierType(syntax: String) {
  override def toString: String = syntax
}

/** Greedy [[weaponregex.model.regextree.QuantifierType]]
  */
case object GreedyQuantifier extends QuantifierType("")

/** Reluctant [[weaponregex.model.regextree.QuantifierType]]
  */
case object ReluctantQuantifier extends QuantifierType("?")

/** Possessive [[weaponregex.model.regextree.QuantifierType]]
  */
case object PossessiveQuantifier extends QuantifierType("+")

/** Long quantifier node
  * @param expr The regex that is being quantified
  * @param min The minimum number of repetition
  * @param max The maximum number of repetitions
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  * @param quantifierType The type of the quantifier: greedy, reluctant, or possessive
  * @param isExact `true` if used to represent an exact number of repetitions (e.g. {1}), `false` otherwise (e.g. {1,2} or {1,})
  * @note This class constructor is private, instances must be created using the companion [[weaponregex.model.regextree.Quantifier]] object
  */
case class Quantifier private (
    expr: RegexTree,
    min: Int,
    max: Int,
    override val location: Location,
    quantifierType: QuantifierType,
    isExact: Boolean
) extends Node(
      Seq(expr),
      location,
      postfix = s"{$min"
        + (if (isExact) "" else "," + (if (max < 0) "" else max))
        + s"}$quantifierType"
    )

/** Companion object for [[weaponregex.model.regextree.Quantifier]] class
  */
object Quantifier {

  /** Infinity will be represented as negatives, default to -1
    */
  val Infinity: Int = -1

  /** Exact quantifier (e.g. {1}) factory method
    * Create an exact [[weaponregex.model.regextree.Quantifier]] instance
    * @param expr The regex that is being quantified
    * @param exact The exact number of repetition
    * @param location The [[weaponregex.model.Location]] of the node in the regex string
    * @param quantifierType The type of the quantifier: greedy, reluctant, or possessive
    * @return A [[weaponregex.model.regextree.Quantifier]] instance
    */
  def apply(
      expr: RegexTree,
      exact: Int,
      location: Location,
      quantifierType: QuantifierType
  ): Quantifier = Quantifier(expr, exact, exact, location, quantifierType, isExact = true)

  /** Range quantifier (e.g. {1,2} or {1,}) factory method
    * @param expr The regex that is being quantified
    * @param min The minimum number of repetition
    * @param max The maximum number of repetitions
    * @param location The [[weaponregex.model.Location]] of the node in the regex string
    * @param quantifierType The type of the quantifier: greedy, reluctant, or possessive
    * @return A [[weaponregex.model.regextree.Quantifier]] instance
    */
  def apply(
      expr: RegexTree,
      min: Int,
      max: Int,
      location: Location,
      quantifierType: QuantifierType
  ): Quantifier = Quantifier(expr, min, max, location, quantifierType, isExact = false)
}

/** Shorthand notation zero or one `?` quantifier node
  * @param expr The regex that is being quantified
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  * @param quantifierType The type of the quantifier: greedy, reluctant, or possessive
  */
case class ZeroOrOne(
    expr: RegexTree,
    override val location: Location,
    quantifierType: QuantifierType
) extends Node(Seq(expr), location, postfix = s"?$quantifierType")

/** Shorthand notation zero or more `*` quantifier node
  * @param expr The regex that is being quantified
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  * @param quantifierType The type of the quantifier: greedy, reluctant, or possessive
  */
case class ZeroOrMore(
    expr: RegexTree,
    override val location: Location,
    quantifierType: QuantifierType
) extends Node(Seq(expr), location, postfix = s"*$quantifierType")

/** Shorthand notation one or more `+` quantifier node
  * @param expr The regex that is being quantified
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  * @param quantifierType The type of the quantifier: greedy, reluctant, or possessive
  */
case class OneOrMore(
    expr: RegexTree,
    override val location: Location,
    quantifierType: QuantifierType
) extends Node(Seq(expr), location, postfix = s"+$quantifierType")

/** Concatenation node
  * @param nodes The nodes that are being concatenated
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Concat(nodes: Seq[RegexTree], override val location: Location) extends Node(nodes, location)

/** Or node (e.g. `a|b|c`)
  * @param nodes The nodes that are being "or-ed"
  * @param location The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Or(nodes: Seq[RegexTree], override val location: Location) extends Node(nodes, location, sep = "|")
