package weaponregex.mutator

import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.model.Location
import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree.*

/** Mutator for capturing group to non-capturing group modification
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `(abc)` ⟶ `(?:abc)`
  */
object GroupToNCGroup extends TokenMutator {
  override val name: String = "Capturing group to non-capturing group modification"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"${location.show} Modify the capturing group `$original` to non-capturing group `$mutated`"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case group @ Group(_, true, _) => Seq(group.copy(isCapturing = false))
    case _                         => Nil
  }) map (_.build.toMutantBeforeChildrenOf(token))
}

/** Mutator for lookaround constructs (lookahead, lookbehind) negation
  *
  * ''Mutation level(s):'' 1, 2, 3
  * @example
  *   `(?=abc)` ⟶ `(?!abc)`
  */
object LookaroundNegation extends TokenMutator {
  override val name: String = "Lookaround constructs (lookahead, lookbehind) negation"
  override val levels: Seq[Int] = Seq(1, 2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"${location.show} Negate the lookaround construct `$original` to `$mutated`"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case la: Lookaround => Seq(la.copy(isPositive = !la.isPositive))
    case _              => Nil
  }) map (_.build.toMutantBeforeChildrenOf(token))
}
