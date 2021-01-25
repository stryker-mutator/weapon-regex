package weaponregex.mutator

import weaponregex.model.mutation.TokenMutator
import weaponregex.model.regextree._
import weaponregex.`extension`.StringExtension._

/** Negate character class
  *
  * ''Mutation level(s):'' 1
  * @example `\d` ⟶ `\D`
  */
object PredefCharClassNegation extends TokenMutator {
  override val name = "Predefined Character Class Negation"
  override val levels: Seq[Int] = Seq(1)
  override val description: String = "Negate character class"

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case pdcc: PredefinedCharClass => Seq(pdcc.copy(charClass = pdcc.charClass.toggleCase))
    case _                         => Nil
  }) map (_.build)
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

  override def mutate(token: RegexTree): Seq[String] = token match {
    case pdcc: PredefinedCharClass => Seq(pdcc.charClass)
    case _                         => Nil
  }
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

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case pdcc: PredefinedCharClass =>
      Seq(CharacterClass(Seq(pdcc, pdcc.copy(charClass = pdcc.charClass.toggleCase)), pdcc.location))
    case _ => Nil
  }) map (_.build)
}
