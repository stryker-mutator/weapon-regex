package weaponregex.mutator

import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.model.Location
import weaponregex.model.mutation.{Mutant, TokenMutator}
import weaponregex.model.regextree.*

/** Remove any type of quantifier including `?`, `*`, `+`, and `{n,m}`
  *
  * ''Mutation level(s):'' 1
  * @example
  *   `a*` ⟶ `a`
  */
object QuantifierRemoval extends TokenMutator {
  override val name: String = "Quantifier removal"
  override val levels: Seq[Int] = Seq(1)
  override def description(original: String, mutated: String, location: Location): String =
    s"Remove the quantifier from `$original` to `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case q: ZeroOrOne  => Seq(q.expr)
    case q: ZeroOrMore => Seq(q.expr)
    case q: OneOrMore  => Seq(q.expr)
    case q: Quantifier => Seq(q.expr)
    case _             => Nil
  }) map (_.build.toMutantAfterChildrenOf(token))
}

/** Change quantifier `{n}` to `{0,n}`, and `{n,}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `a{5}` ⟶ `a{0,5}`, `a{5,}`
  */
object QuantifierNChange extends TokenMutator {
  override val name: String = "Quantifier `{n}` change to `{0,n}`, and `{n,}`"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Change quantifier from `$original` to `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case q: Quantifier if q.isExact =>
      Seq(
        q.copy(isExact = false, min = 0, max = q.min),
        q.copy(isExact = false, max = Quantifier.Infinity)
      )
    case _ => Nil
  }) map (_.build.toMutantAfterChildrenOf(token))
}

/** Modify quantifier `{n,}` to `{n-1,}`, and `{n+1,}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `a{5,}` ⟶ `a{4,}`, `a{6,}`
  */
object QuantifierNOrMoreModification extends TokenMutator {
  override val name: String = "Quantifier `{n,}` modification to `{n-1,}`, and `{n+1,}"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Modify quantifier from `$original` to `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case q: Quantifier if !q.isExact && q.max == Quantifier.Infinity =>
      if (q.min < 1) Seq(q.copy(min = q.min + 1))
      else Seq(q.copy(min = q.min - 1), q.copy(min = q.min + 1))
    case _ => Nil
  }) map (_.build.toMutantAfterChildrenOf(token))
}

/** Change quantifier `{n,}` to `{n}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `a{5,}` ⟶ `a{5}`
  */
object QuantifierNOrMoreChange extends TokenMutator {
  override val name: String = "Quantifier `{n,}` change to `{n}`"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Change quantifier from `$original` to `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case q: Quantifier if !q.isExact && q.max == Quantifier.Infinity => Seq(q.copy(isExact = true))
    case _                                                           => Nil
  }) map (_.build.toMutantAfterChildrenOf(token))
}

/** Modify quantifier `{n,m}` to `{n-1,m}`, `{n+1,m}`, `{n,m-1}`, and `{n,m+1}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `a{5,10}` ⟶ `a{4,10}`, `a{6,10}`, `a{5,9}`, `a{5,11}`
  */
object QuantifierNMModification extends TokenMutator {
  override val name: String = "Quantifier `{n,m}` modification"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Change quantifier from `$original` to `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case q: Quantifier if !q.isExact && q.max != Quantifier.Infinity =>
      (q.min, q.max) match {
        case (0, 0) =>
          Seq(q.copy(max = 1))
        case (0, m) =>
          Seq(
            q.copy(min = 1),
            q.copy(max = m - 1),
            q.copy(max = m + 1)
          )
        case (n, m) =>
          Seq(
            q.copy(min = n - 1),
            q.copy(min = n + 1),
            q.copy(max = m - 1),
            q.copy(max = m + 1)
          )
      }
    case _ => Nil
  }) map (_.build.toMutantAfterChildrenOf(token))
}

/** Modify quantifier `?`, `*`, `+` to `{n,}`, or `{n,m}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `a*` ⟶ `a{1,1}`, `a{0,0}`, `a{0,2}`
  */
object QuantifierShortModification extends TokenMutator {
  override val name: String = "Short quantifier modification to `{n,}`, or `{n,m}`"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Change quantifier from `$original` to `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case q: ZeroOrOne =>
      Seq(
        Quantifier(q.expr, min = 1, max = 1, q.location, q.quantifierType),
        Quantifier(q.expr, min = 0, max = 0, q.location, q.quantifierType),
        Quantifier(q.expr, min = 0, max = 2, q.location, q.quantifierType)
      )
    case q: ZeroOrMore =>
      Seq(Quantifier(q.expr, min = 1, max = Quantifier.Infinity, q.location, q.quantifierType))
    case q: OneOrMore =>
      Seq(
        Quantifier(q.expr, min = 0, max = Quantifier.Infinity, q.location, q.quantifierType),
        Quantifier(q.expr, min = 2, max = Quantifier.Infinity, q.location, q.quantifierType)
      )
    case _ => Nil
  }) map (_.build.toMutantAfterChildrenOf(token))
}

/** Change quantifier `*`, `+` to `{n}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example
  *   `a*` ⟶ `a{0}`
  */
object QuantifierShortChange extends TokenMutator {
  override val name: String = "Short quantifier `*` and `+` change to `{n}`"
  override val levels: Seq[Int] = Seq(2, 3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Change quantifier from `$original` to `$mutated` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case q: ZeroOrMore =>
      Seq(Quantifier(q.expr, exact = 0, q.location, q.quantifierType))
    case q: OneOrMore =>
      Seq(Quantifier(q.expr, exact = 1, q.location, q.quantifierType))
    case _ => Nil
  }) map (_.build.toMutantAfterChildrenOf(token))
}

/** Add reluctant quantifier type to greedy quantifier
  *
  * ''Mutation level(s):'' 3
  * @example
  *   `a+` ⟶ `a+?`
  */
object QuantifierReluctantAddition extends TokenMutator {
  override val name: String = "Quantifier reluctant addition"
  override val levels: Seq[Int] = Seq(3)
  override def description(original: String, mutated: String, location: Location): String =
    s"Add reluctant quantifier type to `$original` at ${location.pretty}"

  override def mutate(token: RegexTree): Seq[Mutant] = (token match {
    case q: ZeroOrOne if q.quantifierType == GreedyQuantifier =>
      Seq(q.copy(quantifierType = ReluctantQuantifier))
    case q: ZeroOrMore if q.quantifierType == GreedyQuantifier =>
      Seq(q.copy(quantifierType = ReluctantQuantifier))
    case q: OneOrMore if q.quantifierType == GreedyQuantifier =>
      Seq(q.copy(quantifierType = ReluctantQuantifier))
    case q: Quantifier if q.quantifierType == GreedyQuantifier =>
      Seq(q.copy(quantifierType = ReluctantQuantifier))
    case _ => Nil
  }) map (_.build.toMutantAfterChildrenOf(token))
}
