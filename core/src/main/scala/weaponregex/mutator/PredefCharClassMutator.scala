package weaponregex.mutator

import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.extension.StringExtension.StringStylingExtension
import weaponregex.model.Location
import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree.*

/** Negate a predefined character class
  *
  * ''Mutation level(s):'' 1
  * @example
  *   `\d` ⟶ `\D`
  */
object PredefCharClassNegation extends TokenMutator {
  override val name = "Predefined character class negation"
  override val levels: Seq[Int] = Seq(1)
  override val description: String = "Negate a predefined character class"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass => Seq(pdcc.copy(charClass = pdcc.charClass.toggleCase))
    case _                         => Nil
  }) map (_.build.toMutantOf(token))
}

/** Nullify a predefined character class by removing the escape character `\`
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `\d` ⟶ `d`
  */
object PredefCharClassNullification extends TokenMutator {
  override val name = "Predefined character class nullification"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String = """Nullify a predefined character class by removing the escape character `\`"""

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass => Seq(pdcc.charClass)
    case _                         => Nil
  }) map (_.toMutantOf(token))
}

/** Add the negation of a predefined character class to match any character
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `\d` ⟶ `[\d\D]`
  */
object PredefCharClassAnyChar extends TokenMutator {
  override val name = "Predefined character class to character class with its negation"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String =
    "Add the negation of a predefined character class to match any character"

  override def describeMutation(token: RegexTree, location: Location): String =
    s"Add the negation of a predefined character class at ${location.start} to match any character"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pdcc: PredefinedCharClass =>
      Seq(CharacterClass(Seq(pdcc, pdcc.copy(charClass = pdcc.charClass.toggleCase)), pdcc.location))
    case _ => Nil
  }) map (_.build.toMutantOf(token))
}

/** Negate a POSIX character class
  *
  * ''Mutation level(s):'' 1
  * @example
  *   `\p{Alpha}` ⟶ `\P{Alpha}`
  */
object POSIXCharClassNegation extends TokenMutator {
  override val name = "POSIX character class negation"
  override val levels: Seq[Int] = Seq(1)
  override val description: String = "Negate a POSIX character class"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case pcc: POSIXCharClass => Seq(pcc.copy(isPositive = !pcc.isPositive))
    case _                   => Nil
  }) map (_.build.toMutantBeforeChildrenOf(token))
}
