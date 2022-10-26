package weaponregex.mutator

import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree.*

/** Remove a beginning of line character `^`
  *
  * ''Mutation level(s):'' 1, 2, 3
  * @example
  *   `^a` ⟶ `a`
  */
object BOLRemoval extends TokenMutator {
  override val name: String = "Beginning of line character `^` removal"
  override val levels: Seq[Int] = Seq(1, 2, 3)
  override val description: String = "Remove a beginning of line character `^`"

  override def mutate(token: RegexTree): Seq[Mutant] = token match {
    case node: Node =>
      node.children flatMap {
        case child: BOL => Seq(token.buildWhile(_ ne child).toMutantOf(child))
        case _          => Nil
      }
    case _ => Nil
  }
}

/** Remove an end of line character `$`
  *
  * ''Mutation level(s):'' 1, 2, 3
  * @example
  *   `a$` ⟶ `a`
  */
object EOLRemoval extends TokenMutator {
  override val name: String = "End of line character `$` removal"
  override val levels: Seq[Int] = Seq(1, 2, 3)
  override val description: String = "Remove an end of line character `$`"

  override def mutate(token: RegexTree): Seq[Mutant] = token match {
    case node: Node =>
      node.children flatMap {
        case child: EOL => Seq(token.buildWhile(_ ne child).toMutantOf(child))
        case _          => Nil
      }
    case _ => Nil
  }
}

/** Change a beginning of line `^` to a beginning of input `\A`
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `^a` ⟶ `\Aa`
  */
object BOL2BOI extends TokenMutator {
  override val name: String = """Beginning of line `^` to beginning of input `\A`"""
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String = """Change a beginning of line `^` to a beginning of input `\A`"""

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case _: BOL => Seq(Boundary("A", token.location))
    case _      => Nil
  }) map (_.build.toMutantOf(token))
}

/** Change an end of line `$` to an end of input `\z`
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `a$` ⟶ `a\z`
  */
object EOL2EOI extends TokenMutator {
  override val name: String = """End of line `$` to end of input `\z`"""
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String = """Change an end of line `$` to an end of input `\z`"""

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case _: EOL => Seq(Boundary("z", token.location))
    case _      => Nil
  }) map (_.build.toMutantOf(token))
}
