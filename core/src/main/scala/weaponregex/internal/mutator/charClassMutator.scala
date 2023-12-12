package weaponregex.internal.mutator

import weaponregex.internal.TokenMutator
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.internal.model.regextree.*
import weaponregex.model.Location
import weaponregex.model.mutation.Mutant

/** Mutator for character class negation
  *
  * ''Mutation level(s):'' 1
  * @example
  *   `[abc]` ⟶ `[^abc]`
  */
object CharClassNegation extends TokenMutator {
  override val name = "Character class negation"
  override val levels: Seq[Int] = Seq(1)
  override def description(original: String, mutated: String, location: Location): String =
    s"${location.show} Negate the character class `$original` to `$mutated`"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case cc: CharacterClass => Seq(cc.copy(isPositive = !cc.isPositive))
    case _                  => Nil
  }) map (_.build.toMutantBeforeChildrenOf(token))
}

/** Mutator for character class child removal
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `[abc]` ⟶ `[ab]`, `[ac]`, `[bc]`
  */
object CharClassChildRemoval extends TokenMutator {
  override val name: String = "Character class child removal"
  override val levels: Seq[Int] = Seq(2, 3)

  override def mutate(token: RegexTree): Seq[Mutant] = {
    def _mutate(token: Node): Seq[Mutant] = token.children map (child =>
      token
        .buildWhile(_ ne child)
        .toMutantOf(
          child,
          description = Some(
            s"${child.location.show} Remove the child `${child.build}` from the character class `${token.build}`"
          )
        )
    )

    token match {
      case cc: CharacterClass if cc.children.length > 1      => _mutate(cc)
      case cc: CharacterClassNaked if cc.children.length > 1 => _mutate(cc)
      case _                                                 => Nil
    }
  }
}

/** Mutator for character class to `[\w\W]` change"""
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `[abc]` ⟶ `[\w\W]`
  */
object CharClassAnyChar extends TokenMutator {
  override val name = """Character class to `[\w\W]` change"""
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"${location.show} Change the character class `$original` to match any character `[\\w\\W]`"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case cc: CharacterClass =>
      Seq(
        CharacterClass(Seq(PredefinedCharClass("w", cc.location), PredefinedCharClass("W", cc.location)), cc.location)
      )
    case _ => Nil
  }) map (_.build.toMutantOf(token))
}

/** Mutator for character class range modification
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
        description = Some(
          s"${range.location.show} ${if (isIncrease) "Increase" else "Decrease"} once the ${if (isLeft) s"lower limit $l"
            else s"upper limit $r"} of the range `${range.build}`"
        )
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
          case (_, _) =>
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
