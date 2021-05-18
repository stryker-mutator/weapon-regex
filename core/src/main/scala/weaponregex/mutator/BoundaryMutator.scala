package weaponregex.mutator

import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree._
import weaponregex.`extension`.RegexTreeExtension.RegexTreeStringBuilder

/** Remove beginning of line character `^`
  *
  * ''Mutation level(s):'' 1, 2, 3
  * @example `^a` ⟶ `a`
  */
object BOLRemoval extends TokenMutator {
  override val name: String = "Beginning of line character `^` removal"
  override val levels: Seq[Int] = Seq(1, 2, 3)
  override val description: String = "Remove beginning of line character `^`"

  override def mutate(token: RegexTree): Seq[Mutant] = token.children.foldLeft(Seq.empty[Mutant])((mutants, child) =>
    child match {
      case _: BOL => mutants :+ token.buildWhile(_ ne child).toMutantOf(child)
      case _      => mutants
    }
  )
}

/** Remove end of line character `$`
  *
  * ''Mutation level(s):'' 1, 2, 3
  * @example `a$` ⟶ `a`
  */
object EOLRemoval extends TokenMutator {
  override val name: String = "End of line character `$` removal"
  override val levels: Seq[Int] = Seq(1, 2, 3)
  override val description: String = "Remove end of line character `$`"

  override def mutate(token: RegexTree): Seq[Mutant] = token.children.foldLeft(Seq.empty[Mutant])((mutants, child) =>
    child match {
      case _: EOL => mutants :+ token.buildWhile(_ ne child).toMutantOf(child)
      case _      => mutants
    }
  )
}

/** Change beginning of line `^` to beginning of input `\A`
  *
  * ''Mutation level(s):'' 2, 3
  * @example `^a` ⟶ `\Aa`
  */
object BOL2BOI extends TokenMutator {
  override val name: String = """Beginning of line `^` to beginning of input `\A`"""
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String = """Change beginning of line `^` to beginning of input `\A`"""

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case _: BOL => Seq(Boundary("A", token.location))
    case _      => Nil
  }) map (_.build.toMutantOf(token))
}

/** Change end of line `$` to end pf input `\z`
  *
  * ''Mutation level(s):'' 2, 3
  * @example `a$` ⟶ `a\z`
  */
object EOL2EOI extends TokenMutator {
  override val name: String = """End of line `$` to end of input `\z`"""
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String = """Change end of line `$` to end of input `\z`"""

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case _: EOL => Seq(Boundary("z", token.location))
    case _      => Nil
  }) map (_.build.toMutantOf(token))
}
