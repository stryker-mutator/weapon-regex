package weaponregex.internal.model.regextree

import weaponregex.model.Location

/** The enumeration of the quantifier type
  *
  * @param syntax
  *   The syntax used to represent the quantifier type
  */
sealed abstract class QuantifierType(syntax: String) {
  override def toString: String = syntax
}

/** Greedy [[weaponregex.internal.model.regextree.QuantifierType]]
  */
case object GreedyQuantifier extends QuantifierType("")

/** Reluctant [[weaponregex.internal.model.regextree.QuantifierType]]
  */
case object ReluctantQuantifier extends QuantifierType("?")

/** Possessive [[weaponregex.internal.model.regextree.QuantifierType]]
  */
case object PossessiveQuantifier extends QuantifierType("+")

/** Long quantifier node
  * @param expr
  *   The regex that is being quantified
  * @param min
  *   The minimum number of repetition
  * @param max
  *   The maximum number of repetitions
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  * @param quantifierType
  *   The type of the quantifier: greedy, reluctant, or possessive
  * @param isExact
  *   `true` if used to represent an exact number of repetitions (e.g. {1}), `false` otherwise (e.g. {1,2} or {1,})
  * @note
  *   This class constructor is private, instances must be created using the companion
  *   [[weaponregex.internal.model.regextree.Quantifier]] object
  */
case class Quantifier protected[weaponregex] (
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

/** Companion object for [[weaponregex.internal.model.regextree.Quantifier]] class
  */
object Quantifier {

  /** Infinity will be represented as negatives, default to -1
    */
  val Infinity: Int = -1

  /** Exact quantifier (e.g. {1}) factory method Create an exact [[weaponregex.internal.model.regextree.Quantifier]]
    * instance
    * @param expr
    *   The regex that is being quantified
    * @param exact
    *   The exact number of repetition
    * @param location
    *   The [[weaponregex.model.Location]] of the node in the regex string
    * @param quantifierType
    *   The type of the quantifier: greedy, reluctant, or possessive
    * @return
    *   A [[weaponregex.internal.model.regextree.Quantifier]] instance
    */
  def apply(
      expr: RegexTree,
      exact: Int,
      location: Location,
      quantifierType: QuantifierType
  ): Quantifier = Quantifier(expr, exact, exact, location, quantifierType, isExact = true)

  /** Range quantifier (e.g. {1,2} or {1,}) factory method
    * @param expr
    *   The regex that is being quantified
    * @param min
    *   The minimum number of repetition
    * @param max
    *   The maximum number of repetitions
    * @param location
    *   The [[weaponregex.model.Location]] of the node in the regex string
    * @param quantifierType
    *   The type of the quantifier: greedy, reluctant, or possessive
    * @return
    *   A [[weaponregex.internal.model.regextree.Quantifier]] instance
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
  * @param expr
  *   The regex that is being quantified
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  * @param quantifierType
  *   The type of the quantifier: greedy, reluctant, or possessive
  */
case class ZeroOrOne(
    expr: RegexTree,
    override val location: Location,
    quantifierType: QuantifierType
) extends Node(Seq(expr), location, postfix = s"?$quantifierType")

/** Shorthand notation zero or more `*` quantifier node
  * @param expr
  *   The regex that is being quantified
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  * @param quantifierType
  *   The type of the quantifier: greedy, reluctant, or possessive
  */
case class ZeroOrMore(
    expr: RegexTree,
    override val location: Location,
    quantifierType: QuantifierType
) extends Node(Seq(expr), location, postfix = s"*$quantifierType")

/** Shorthand notation one or more `+` quantifier node
  * @param expr
  *   The regex that is being quantified
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  * @param quantifierType
  *   The type of the quantifier: greedy, reluctant, or possessive
  */
case class OneOrMore(
    expr: RegexTree,
    override val location: Location,
    quantifierType: QuantifierType
) extends Node(Seq(expr), location, postfix = s"+$quantifierType")
