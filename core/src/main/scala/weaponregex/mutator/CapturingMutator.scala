package weaponregex.mutator

import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.model.Location
import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree._

/** Modify capturing group to non-capturing group
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `(abc)` ⟶ `(?:abc)`
  */
object GroupToNCGroup extends TokenMutator {
  override val name: String = "Capturing group to non-capturing group"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Modify capturing group `$original` to non-capturing group `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case group @ Group(_, true, _) => Seq(group.copy(isCapturing = false))
    case _                         => Nil
  }) map (_.build.toMutantBeforeChildrenOf(token))
}

/** Negate lookaround (lookahead, lookbehind) constructs
  *
  * ''Mutation level(s):'' 1, 2, 3
  * @example
  *   `(?=abc)` ⟶ `(?!abc)`
  */
object LookaroundNegation extends TokenMutator {
  override val name: String = "Lookaround constructs (lookahead, lookbehind) negation"
  override val levels: Seq[Int] = Seq(1, 2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Negate lookaround construct `$original` to `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case la: Lookaround => Seq(la.copy(isPositive = !la.isPositive))
    case _              => Nil
  }) map (_.build.toMutantBeforeChildrenOf(token))
}
