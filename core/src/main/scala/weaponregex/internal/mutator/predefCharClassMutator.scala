package weaponregex.internal.mutator

import weaponregex.internal.TokenMutator
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.internal.extension.StringExtension.StringStylingExtension
import weaponregex.internal.model.regextree.*
import weaponregex.model.Location
import weaponregex.model.mutation.Mutant

/** Mutator for predefined character class negation
  *
  * ''Mutation level(s):'' 1
  * @example
  *   `\d` ⟶ `\D`
  */
object PredefCharClassNegation extends TokenMutator {
  override val name = "Predefined character class negation"
  override val levels: Seq[Int] = Seq(1)
  override def description(original: String, mutated: String, location: Location): String =
    s"${location.show} Negate the predefined character class `$original` to `$mutated`"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass => Seq(pdcc.copy(charClass = pdcc.charClass.toggleCase))
    case _                         => Nil
  }) map (_.build.toMutantOf(token))
}

/** Mutator for predefined character class nullification
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `\d` ⟶ `d`
  */
object PredefCharClassNullification extends TokenMutator {
  override val name = "Predefined character class nullification"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"${location.show} Nullify the predefined character class `$original` by removing the `\\`"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass => Seq(pdcc.charClass)
    case _                         => Nil
  }) map (_.toMutantOf(token))
}

/** Mutator for predefined character class to character class with its negation change
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `\d` ⟶ `[\d\D]`
  */
object PredefCharClassAnyChar extends TokenMutator {
  override val name = "Predefined character class to character class with its negation change"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"${location.show} Change the predefined character class `$original` to a character class with its negation `$mutated` to match any character"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass =>
      Seq(CharacterClass(Seq(pdcc, pdcc.copy(charClass = pdcc.charClass.toggleCase)), pdcc.location))
    case _ => Nil
  }) map (_.build.toMutantOf(token))
}

/** Mutator for Unicode character class negation
  *
  * ''Mutation level(s):'' 1
  * @example
  *   `\p{Alpha}` ⟶ `\P{Alpha}`
  */
object UnicodeCharClassNegation extends TokenMutator {
  override val name = "Unicode character class negation"
  override val levels: Seq[Int] = Seq(1)
  override def description(original: String, mutated: String, location: Location): String =
    s"${location.show} sNegate Unicode character class `$original` to `$mutated`"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case ucc: UnicodeCharClass => Seq(ucc.copy(isPositive = !ucc.isPositive))
    case _                     => Nil
  }) map (g => g.build.toMutantBeforeChildrenOf(token, replacement = g.prefix))
}
