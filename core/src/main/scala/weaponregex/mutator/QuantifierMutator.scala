package weaponregex.mutator

import weaponregex.model.mutation.TokenMutator
import weaponregex.model.regextree._

/** Remove any type of quantifier including `?`, `*`, `+`, and `{n,m}`
  *
  * ''Mutation level(s):'' 1
  * @example `a*` ⟶ `a`
  */
object QuantifierRemoval extends TokenMutator {
  override val name: String = "Quantifier removal"
  override val levels: Seq[Int] = Seq(1)
  override val description: String =
    "Remove greedy, reluctant, and possessive quantifier including `?`, `*`, `+`, and `{n,m}`"

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case q: ZeroOrOne  => Seq(q.expr)
    case q: ZeroOrMore => Seq(q.expr)
    case q: OneOrMore  => Seq(q.expr)
    case q: Quantifier => Seq(q.expr)
    case _             => Nil
  }) map (_.build)
}

/** Change quantifier `{n}` to `{0,n}`, and `{n,}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example `a{5}` ⟶ `a{0,5}`, ``a{}5,
  */
object QuantifierNChange extends TokenMutator {
  override val name: String = "Quantifier `{n}` change"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String =
    "Change quantifier `{n}` to `{0,n}`, and `{n,}`"

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case q: Quantifier if q.isExact =>
      Seq(
        q.copy(isExact = false, min = 0, max = q.min),
        q.copy(isExact = false, max = Quantifier.Infinity)
      )
    case _ => Nil
  }) map (_.build)
}

/** Modify quantifier `{n,}` to `{n-1,}`, and `{n+1,}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example `a{5,}` ⟶ `a{4,}`, `a{6,}`
  */
object QuantifierNOrMoreModification extends TokenMutator {
  override val name: String = "Quantifier `{n,}` modification"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String =
    "Modify quantifier `{n,}` to `{n-1,}`, and `{n+1,}`"

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case q: Quantifier if !q.isExact && q.max == Quantifier.Infinity =>
      if (q.min < 1) Seq(q.copy(min = q.min + 1))
      else Seq(q.copy(min = q.min - 1), q.copy(min = q.min + 1))
    case _ => Nil
  }) map (_.build)
}

/** Change quantifier `{n,}` to `{n}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example `a{5,}` ⟶ `a{5}`
  */
object QuantifierNOrMoreChange extends TokenMutator {
  override val name: String = "Quantifier `{n,}` change"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String =
    "Change quantifier `{n,}` to `{n}`"

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case q: Quantifier if !q.isExact && q.max == Quantifier.Infinity => Seq(q.copy(isExact = true))
    case _                                                           => Nil
  }) map (_.build)
}

/** Modify quantifier `{n,m}` to `{n-1,m}`, `{n+1,m}`, `{n,m-1}`, and `{n,m+1}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example `a{5,10}` ⟶ `a{4,10}`, `a{6,10}`, `a{5,9}`, `a{5,11}`
  */
object QuantifierNMModification extends TokenMutator {
  override val name: String = "Quantifier `{n,m}` modification"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String =
    "Modify quantifier `{n,m}` to `{n-1,m}`, `{n+1,m}`, `{n,m-1}`, and `{n,m+1}`"

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case q: Quantifier if !q.isExact && q.max != Quantifier.Infinity =>
      (q.min, q.max) match {
        case (n, m) if n < 0 && m < 0 => Nil
        case (0, 0)                   => Seq(q.copy(max = 1))
        case (0, m)                   => Seq(q.copy(min = 1), q.copy(max = m - 1), q.copy(max = m + 1))
        case (n, m) =>
          Seq(
            q.copy(min = n - 1),
            q.copy(min = n + 1),
            q.copy(max = m - 1),
            q.copy(max = m + 1)
          )
      }
    case _ => Nil
  }) map (_.build)
}

/** Modify quantifier `?`, `*`, `+` to `{n,}`, or `{n,m}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example `a*` ⟶ `a{1,1}`, `a{0,0}`, `a{0,2}`
  */
object QuantifierShortModification extends TokenMutator {
  override val name: String = "Short quantifier modification"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String =
    "Modify quantifier `?`, `*`, `+` to `{n,}`, or `{n,m}`"

  override def mutate(token: RegexTree): Seq[String] = (token match {
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
  }) map (_.build)
}

/** Change quantifier `*`, `+` to `{n}`
  *
  * ''Mutation level(s):'' 2, 3
  * @example `a*` ⟶ `a{0}`
  */
object QuantifierShortChange extends TokenMutator {
  override val name: String = "Short quantifier change"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String =
    "Change quantifier `*`, `+` to `{n}`"

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case q: ZeroOrMore =>
      Seq(Quantifier(q.expr, exact = 0, q.location, q.quantifierType))
    case q: OneOrMore =>
      Seq(Quantifier(q.expr, exact = 1, q.location, q.quantifierType))
    case _ => Nil
  }) map (_.build)
}

/** Add reluctant quantifier type to greedy quantifier
  *
  * ''Mutation level(s):'' 3
  * @example `a+` ⟶ `a+?`
  */
object QuantifierReluctantAddition extends TokenMutator {
  override val name: String = "Quantifier reluctant addition"
  override val levels: Seq[Int] = Seq(3)
  override val description: String =
    "Add reluctant quantifier type to greedy quantifier"

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case q: ZeroOrOne if q.quantifierType == GreedyQuantifier =>
      Seq(q.copy(quantifierType = ReluctantQuantifier))
    case q: ZeroOrMore if q.quantifierType == GreedyQuantifier =>
      Seq(q.copy(quantifierType = ReluctantQuantifier))
    case q: OneOrMore if q.quantifierType == GreedyQuantifier =>
      Seq(q.copy(quantifierType = ReluctantQuantifier))
    case q: Quantifier if q.quantifierType == GreedyQuantifier =>
      Seq(q.copy(quantifierType = ReluctantQuantifier))
    case _ => Nil
  }) map (_.build)
}
