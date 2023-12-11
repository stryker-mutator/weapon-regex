package weaponregex.mutator

import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.extension.StringExtension.StringStylingExtension
import weaponregex.model.Location
import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree.*

/** Negate predefined character class
  *
  * ''Mutation level(s):'' 1
  * @example
  *   `\d` ⟶ `\D`
  */
object PredefCharClassNegation extends TokenMutator {
  override val name = "Predefined character class negation"
  override val levels: Seq[Int] = Seq(1)
  override def description(original: String, mutated: String, location: Location): String =
    s"Negate predefined character class `$original` to `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass => Seq(pdcc.copy(charClass = pdcc.charClass.toggleCase))
    case _                         => Nil
  }) map (_.build.toMutantOf(token))
}

/** Nullify a predefined character class by removing the `\`
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `\d` ⟶ `d`
  */
object PredefCharClassNullification extends TokenMutator {
  override val name = "Predefined Character Class Nullification"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Nullify predefined character class `$original` by removing the `\\` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass => Seq(pdcc.charClass)
    case _                         => Nil
  }) map (_.toMutantOf(token))
}

/** "Add the negation of that predefined character class to match any character `[\\w\\W]`"
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `\d` ⟶ `[\d\D]`
  */
object PredefCharClassAnyChar extends TokenMutator {
  override val name = "Predefined Character Class to character class with its negation"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Add the negation of that predefined character class `$original` to match any character `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass =>
      Seq(CharacterClass(Seq(pdcc, pdcc.copy(charClass = pdcc.charClass.toggleCase)), pdcc.location))
    case _ => Nil
  }) map (_.build.toMutantOf(token))
}

/** Negate POSIX character class
  *
  * ''Mutation level(s):'' 1
  * @example
  *   `\p{Alpha}` ⟶ `\P{Alpha}`
  */
object POSIXCharClassNegation extends TokenMutator {
  override val name = "POSIX Character Class Negation"
  override val levels: Seq[Int] = Seq(1)
  override def description(original: String, mutated: String, location: Location): String =
    s"Negate POSIX character class `$original` to `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pcc: POSIXCharClass => Seq(pcc.copy(isPositive = !pcc.isPositive))
    case _                   => Nil
  }) map (_.build.toMutantBeforeChildrenOf(token))
}
