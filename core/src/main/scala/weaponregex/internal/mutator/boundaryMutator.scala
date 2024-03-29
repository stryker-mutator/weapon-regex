package weaponregex.internal.mutator

import weaponregex.internal.TokenMutator
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.internal.model.regextree.*
import weaponregex.model.Location
import weaponregex.model.mutation.Mutant

/** Mutator for beginning of line character `^` removal
  *
  * ''Mutation level(s):'' 1, 2, 3
  * @example
  *   `^a` ⟶ `a`
  */
object BOLRemoval extends TokenMutator {
  override val name: String = "Beginning of line character `^` removal"
  override val levels: Seq[Int] = Seq(1, 2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    location.show + " Remove the beginning of line character `^`"

  override def mutate(token: RegexTree): Seq[Mutant] = token match {
    case node: Node =>
      node.children flatMap {
        case child: BOL => Seq(token.buildWhile(_ ne child).toMutantOf(child, replacement = ""))
        case _          => Nil
      }
    case _ => Nil
  }
}

/** Mutator for end of line character `$` removal
  *
  * ''Mutation level(s):'' 1, 2, 3
  * @example
  *   `a$` ⟶ `a`
  */
object EOLRemoval extends TokenMutator {
  override val name: String = "End of line character `$` removal"
  override val levels: Seq[Int] = Seq(1, 2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    location.show + " Remove the end of line character `$`"

  override def mutate(token: RegexTree): Seq[Mutant] = token match {
    case node: Node =>
      node.children flatMap {
        case child: EOL => Seq(token.buildWhile(_ ne child).toMutantOf(child, replacement = ""))
        case _          => Nil
      }
    case _ => Nil
  }
}

/** Mutator for beginning of line `^` to beginning of input `\A` change
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `^a` ⟶ `\Aa`
  */
object BOL2BOI extends TokenMutator {
  override val name: String = """Beginning of line `^` to beginning of input `\A` change"""
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    location.show + """ Change the beginning of line `^` to beginning of input `\A`"""

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case _: BOL => Seq(Boundary("A", token.location))
    case _      => Nil
  }) map (_.build.toMutantOf(token))
}

/** Mutator for end of line `$` to end of input `\z` change
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `a$` ⟶ `a\z`
  */
object EOL2EOI extends TokenMutator {
  override val name: String = """End of line `$` to end of input `\z` change"""
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    location.show + """ Change the end of line `$` to end of input `\z`"""

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case _: EOL => Seq(Boundary("z", token.location))
    case _      => Nil
  }) map (_.build.toMutantOf(token))
}
