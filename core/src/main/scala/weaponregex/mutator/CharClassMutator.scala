package weaponregex.mutator

import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.model.Location
import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree._

/** Negate character class
  *
  * ''Mutation level(s):'' 1
  * @example
  *   `[abc]` ⟶ `[^abc]`
  */
object CharClassNegation extends TokenMutator {
  override val name = "Character class negation"
  override val levels: Seq[Int] = Seq(1)
  override def description(original: String, mutated: String, location: Location): String =
    s"Negate character class `$original` to `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case cc: CharacterClass => Seq(cc.copy(isPositive = !cc.isPositive))
    case _                  => Nil
  }) map (_.build.toMutantBeforeChildrenOf(token))
}

/** Remove a child from character class
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `[abc]` ⟶ `[ab]`, `[ac]`, `[bc]`
  */
object CharClassChildRemoval extends TokenMutator {
  override val name: String = "Character class child removal"
  override val levels: Seq[Int] = Seq(2, 3)

  override def mutate(token: RegexTree): Seq[Mutant] = {
    def _mutate(token: RegexTree): Seq[Mutant] = token.children map (child =>
      token
        .buildWhile(_ ne child)
        .toMutantOf(
          child,
          description = s"Remove `${child.build}` from the character class `${token.build}` at ${child.location.pretty}"
        )
    )

    token match {
      case cc: CharacterClass if cc.children.length > 1      => _mutate(cc)
      case cc: CharacterClassNaked if cc.children.length > 1 => _mutate(cc)
      case _                                                 => Nil
    }
  }
}

/** Change character class to match any character [\w\W]
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `[abc]` ⟶ `[\w\W]`
  */
object CharClassAnyChar extends TokenMutator {
  override val name = "Character class to character class that parses anything"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Change character class `$original` to match any character `[\\w\\W]` at ${location.pretty}"

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
  * @example
  *   `[b-y]` ⟶ `[a-y]`, `[c-y]`, `[b-x]`, `[b-z]`
  */
object CharClassRangeModification extends TokenMutator {
  override val name = "Character class range modification"
  override val levels: Seq[Int] = Seq(3)

  // [b-y] -> [a-y] or [c-y] or [b-z] or [b-x]
  // [a-y] -> [b-y] or [a-z] or [a-x]
  // [b-z] -> [a-z] or [c-z] or [b-y]
  // [a-z] -> [b-z] or [a-y]
  // [b-b] -> [a-b] or [b-c]
  // [a-a] -> [a-b]
  // [z-z] -> [y-z]
  // same for numbers
  override def mutate(token: RegexTree): Seq[Mutant] = {
    def _mutate(range: Range, isLeft: Boolean, isIncrease: Boolean): Mutant = {
      val l: Char = range.from.char
      val r: Char = range.to.char

      val modifier: Char => Char = if (isIncrease) nextChar else prevChar

      val mutatedRange =
        if (isLeft) range.copy(from = range.from.copy(modifier(l)))
        else range.copy(to = range.to.copy(modifier(r)))

      mutatedRange.build.toMutantOf(
        token,
        description = (if (isIncrease) "Increase" else "Decrease") +
          " once the " + (if (isLeft) "lower" else "upper") + s" limit `$l` of the range `${range.build}`"
      )
    }

    token match {
      case range: Range =>
        (range.from.char, range.to.char) match {
          case (l, r) if !(l.isDigit && r.isDigit) && !(l.isLetter && r.isLetter) => Nil
          case (l, r) if isRightBound(l) && isRightBound(r) =>
            Seq(_mutate(range, isLeft = true, isIncrease = false))
          case (l, r) if isLeftBound(l) && isLeftBound(r) =>
            Seq(_mutate(range, isLeft = false, isIncrease = true))
          case (l, r) if l == r =>
            Seq(
              _mutate(range, isLeft = true, isIncrease = false),
              _mutate(range, isLeft = false, isIncrease = true)
            )
          case (l, r) if isLeftBound(l) && isRightBound(r) =>
            Seq(
              _mutate(range, isLeft = true, isIncrease = true),
              _mutate(range, isLeft = false, isIncrease = false)
            )
          case (l, r) if !isLeftBound(l) && isRightBound(r) =>
            Seq(
              _mutate(range, isLeft = true, isIncrease = false),
              _mutate(range, isLeft = true, isIncrease = true),
              _mutate(range, isLeft = false, isIncrease = false)
            )
          case (l, r) if isLeftBound(l) && !isRightBound(r) =>
            Seq(
              _mutate(range, isLeft = true, isIncrease = true),
              _mutate(range, isLeft = false, isIncrease = false),
              _mutate(range, isLeft = false, isIncrease = true)
            )
          case (l, r) =>
            Seq(
              _mutate(range, isLeft = true, isIncrease = false),
              _mutate(range, isLeft = true, isIncrease = true),
              _mutate(range, isLeft = false, isIncrease = false),
              _mutate(range, isLeft = false, isIncrease = true)
            )
        }
      case _ => Nil
    }
  }

  /** Check if the given character is a left boundary character `0`, `a`, or `A`
    * @param char
    *   Character to be checked
    * @return
    *   `true` if the given character is a left boundary character, `false` otherwise
    */
  def isLeftBound(char: Char): Boolean = "0aA".contains(char)

  /** Check if the given character is a right boundary character `9`, `z`, or `Z`
    * @param char
    *   Character to be checked
    * @return
    *   `true` if the given character is a right boundary character, `false` otherwise
    */
  def isRightBound(char: Char): Boolean = "9zZ".contains(char)

  /** Get the character after the given character, based on Scala character ordering
    * @param char
    *   The given character
    * @return
    *   The character after the given character, based on Scala character ordering
    */
  def nextChar(char: Char): Char = (char + 1).toChar

  /** Get the character before the given character, based on Scala character ordering
    * @param char
    *   The given character
    * @return
    *   The character before the given character, based on Scala character ordering
    */
  def prevChar(char: Char): Char = (char - 1).toChar
}
