package weaponregex.mutator

import weaponregex.model.mutation.TokenMutator
import weaponregex.model.regextree._

/** Modify capturing group to non-capturing group
  *
  * ''Mutation level(s):'' 2, 3
  * @example `(abc)` ⟶ `(?:abc)`
  */
object GroupToNCGroup extends TokenMutator {
  override val name: String = "Capturing group to non-capturing group"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String = "Modify capturing group to non-capturing group"

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case group @ Group(_, true, _) => Seq(group.copy(isCapturing = false))
    case _                         => Nil
  }) map (_.build)
}

/** Negate lookaround (lookahead, lookbehind) constructs
  *
  * ''Mutation level(s):'' 1, 2, 3
  * @example `(?=abc)` ⟶ `(?!abc)`
  */
object LookaroundNegation extends TokenMutator {
  override val name: String = "Lookaround constructs negation"
  override val levels: Seq[Int] = Seq(1, 2, 3)
  override val description: String = "Negate lookaround constructs (lookahead, lookbehind)"

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case la: Lookaround => Seq(la.copy(isPositive = !la.isPositive))
    case _              => Nil
  }) map (_.build)
}
