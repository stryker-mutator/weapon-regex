package weaponregex.mutator

import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree._

/** Negate character class
  *
  * ''Mutation level(s):'' 1
  * @example `[abc]` ⟶ `[^abc]`
  */
object CharClassNegation extends TokenMutator {
  override val name = "Character Class Negation"
  override val levels: Seq[Int] = Seq(1)
  override val description: String = "Negate character class"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case cc: CharacterClass => Seq(cc.copy(isPositive = !cc.isPositive))
    case _                  => Nil
  }) map (_.build.toMutantBeforeChildrenOf(token))
}

/** Remove a child from character class
  *
  * ''Mutation level(s):'' 2, 3
  * @example `[abc]` ⟶ `[ab]`, `[ac]`, `[bc]`
  */
object CharClassChildRemoval extends TokenMutator {
  override val name: String = "Remove a child from the character class"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String = "Remove a child character class"

  override def mutate(token: RegexTree): Seq[Mutant] = token match {
    case cc: CharacterClass if cc.children.length > 1 =>
      cc.children map (child => cc.buildWhile(_ ne child).toMutantOf(child))
    case cc: CharacterClassNaked if cc.children.length > 1 =>
      cc.children map (child => cc.buildWhile(_ ne child).toMutantOf(child))
    case _ => Nil
  }
}

/** Change character class to match any character [\w\W]
  *
  * ''Mutation level(s):'' 2, 3
  * @example `[abc]` ⟶ `[\w\W]`
  */
object CharClassAnyChar extends TokenMutator {
  override val name = "Character Class to character class that parses anything"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String = "Change character class to match any character [\\w\\W]"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case cc: CharacterClass =>
      Seq(
        CharacterClass(Seq(PredefinedCharClass("w", cc.location), PredefinedCharClass("W", cc.location)), cc.location)
      )
    case _ => Nil
  }) map (_.build.toMutantOf(token))
}

/** Modify the range inside the character class by increasing or decreasing once
  *
  * ''Mutation level(s):'' 3
  * @example `[b-y]` ⟶ `[a-y]`, `[c-y]`, `[b-x]`, `[b-z]`
  */
object CharClassRangeModification extends TokenMutator {
  override val name = "Modify the range inside the character class"
  override val levels: Seq[Int] = Seq(3)
  override val description: String = "Modify the range inside the character class by increasing or decreasing once"

  // [b-y] -> [a-y] or [c-y] or [b-z] or [b-x]
  // [a-y] -> [b-y] or [a-z] or [a-x]
  // [b-z] -> [a-z] or [c-z] or [b-y]
  // [a-z] -> [b-z] or [a-y]
  // [b-b] -> [a-b] or [b-c]
  // [a-a] -> [a-b]
  // [z-z] -> [y-z]
  // same for numbers
  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case range @ Range(from: Character, to: Character, _) =>
      (from.char, to.char) match {
        case (l, r) if !(l.isDigit && r.isDigit) && !(l.isLetter && r.isLetter) => Nil
        case (l, r) if isRightBound(l) && isRightBound(r)                       => Seq(range.copy(from = from.copy(prevChar(l))))
        case (l, r) if isLeftBound(l) && isLeftBound(r)                         => Seq(range.copy(to = to.copy(nextChar(r))))
        case (l, r) if l == r =>
          Seq(
            range.copy(from = from.copy(prevChar(l))),
            range.copy(to = to.copy(nextChar(r)))
          )
        case (l, r) if isLeftBound(l) && isRightBound(r) =>
          Seq(
            range.copy(from = from.copy(nextChar(l))),
            range.copy(to = to.copy(prevChar(r)))
          )
        case (l, r) if !isLeftBound(l) && isRightBound(r) =>
          Seq(
            range.copy(from = from.copy(prevChar(l))),
            range.copy(from = from.copy(nextChar(l))),
            range.copy(to = to.copy(prevChar(r)))
          )
        case (l, r) if isLeftBound(l) && !isRightBound(r) =>
          Seq(
            range.copy(from = from.copy(nextChar(l))),
            range.copy(to = to.copy(prevChar(r))),
            range.copy(to = to.copy(nextChar(r)))
          )
        case (l, r) =>
          Seq(
            range.copy(from = from.copy(prevChar(l))),
            range.copy(from = from.copy(nextChar(l))),
            range.copy(to = to.copy(prevChar(r))),
            range.copy(to = to.copy(nextChar(r)))
          )
      }
    case _ => Nil
  }) map (_.build.toMutantOf(token))

  /** Check if the given character is a left boundary character `0`, `a`, or `A`
    * @param char Character to be checked
    * @return `true` if the given character is a left boundary character, `false` otherwise
    */
  def isLeftBound(char: Char): Boolean = "0aA".contains(char)

  /** Check if the given character is a right boundary character `9`, `z`, or `Z`
    * @param char Character to be checked
    * @return `true` if the given character is a right boundary character, `false` otherwise
    */
  def isRightBound(char: Char): Boolean = "9zZ".contains(char)

  /** Get the character after the given character, based on Scala character ordering
    * @param char The given character
    * @return The character after the given character, based on Scala character ordering
    */
  def nextChar(char: Char): Char = (char + 1).toChar

  /** Get the character before the given character, based on Scala character ordering
    * @param char The given character
    * @return The character before the given character, based on Scala character ordering
    */
  def prevChar(char: Char): Char = (char - 1).toChar
}
