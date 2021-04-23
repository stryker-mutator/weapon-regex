package weaponregex.mutator

import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree._
import weaponregex.`extension`.StringExtension._

/** Negate predefined character class
  *
  * ''Mutation level(s):'' 1
  * @example `\d` ⟶ `\D`
  */
object PredefCharClassNegation extends TokenMutator {
  override val name = "Predefined Character Class Negation"
  override val levels: Seq[Int] = Seq(1)
  override val description: String = "Negate predefined character class"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass => Seq(pdcc.copy(charClass = pdcc.charClass.toggleCase))
    case _                         => Nil
  }) map (_.build.toMutantOf(token))
}

/** Nullify a predefined character class by removing the `\`
  *
  * ''Mutation level(s):'' 2, 3
  * @example `\d` ⟶ `d`
  */
object PredefCharClassNullification extends TokenMutator {
  override val name = "Predefined Character Class Nullification"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String = "Nullify a predefined character class by removing the `\\`"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass => Seq(pdcc.charClass)
    case _                         => Nil
  }) map (_.toMutantOf(token))
}

/** "Add the negation of that predefined character class to match any character `[\\w\\W]`"
  *
  * ''Mutation level(s):'' 2, 3
  * @example `\d` ⟶ `[\d\D]`
  */
object PredefCharClassAnyChar extends TokenMutator {
  override val name = "Predefined Character Class to character class with its negation"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String =
    "Add the negation of that predefined character class to match any character [\\w\\W]"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass =>
      Seq(CharacterClass(Seq(pdcc, pdcc.copy(charClass = pdcc.charClass.toggleCase)), pdcc.location))
    case _ => Nil
  }) map (_.build.toMutantOf(token))
}

/** Negate POSIX character class
  *
  * ''Mutation level(s):'' 1
  * @example `\d` ⟶ `\D`
  */
object POSIXCharClassNegation extends TokenMutator {
  override val name = "POSIX Character Class Negation"
  override val levels: Seq[Int] = Seq(1)
  override val description: String = "Negate POSIX character class"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pcc: POSIXCharClass => Seq(pcc.copy(isPositive = !pcc.isPositive))
    case _                   => Nil
  }) map (_.build.toMutantOf(token))
}
