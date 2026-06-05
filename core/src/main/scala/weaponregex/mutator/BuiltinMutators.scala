package weaponregex.mutator

import cats.data.{NonEmptyList, NonEmptyMap, NonEmptySet}
import cats.syntax.all.*
import weaponregex.internal.mutator.*
import weaponregex.model.mutation.TokenMutator

/** The object that manages all built-in token mutators
  */
object BuiltinMutators {

  /** Sequence of all built-in token mutators
    */
  val all: NonEmptyList[TokenMutator] = NonEmptyList.of(
    BOLRemoval,
    EOLRemoval,
    BOL2BOI,
    EOL2EOI,
    CharClassNegation,
    CharClassChildRemoval,
    CharClassAnyChar,
    CharClassRangeModification,
    PredefCharClassNegation,
    PredefCharClassNullification,
    PredefCharClassAnyChar,
    UnicodeCharClassNegation,
    QuantifierRemoval,
    QuantifierNChange,
    QuantifierNOrMoreModification,
    QuantifierNOrMoreChange,
    QuantifierNMModification,
    QuantifierShortModification,
    QuantifierShortChange,
    QuantifierReluctantAddition,
    GroupToNCGroup,
    LookaroundNegation
  )

  /** Map from mutator class name to the associating token mutator
    */
  lazy val byName: NonEmptyMap[String, TokenMutator] =
    all.map(mutator => mutator.getClass.getSimpleName.stripSuffix("$") -> mutator).toNem

  /** Map from mutation level number to token mutators in that level
    */
  lazy val byLevel: NonEmptyMap[Int, NonEmptyList[TokenMutator]] =
    all.flatMap(m => m.levels.toNonEmptyList.map(_ -> m)).groupMapNem { case (l, _) => l } { case (_, m) => m }

  final def apply(className: String): Option[TokenMutator] = byName(className)

  final def apply(mutationLevel: Int): Option[NonEmptyList[TokenMutator]] = atLevel(mutationLevel)

  final def apply(mutationLevels: NonEmptySet[Int]): Option[NonEmptyList[TokenMutator]] = atLevels(mutationLevels)

  /** Get all the token mutators in the given mutation level
    * @param mutationLevel
    *   Mutation level number
    * @return
    *   Sequence of all the tokens mutators in that level, if any
    */
  def atLevel(mutationLevel: Int): Option[NonEmptyList[TokenMutator]] = byLevel(mutationLevel)

  /** Get all the token mutators in the given mutation levels
    * @param mutationLevels
    *   Mutation level numbers
    * @return
    *   Sequence of all the tokens mutators in that levels, if any
    */
  def atLevels(mutationLevels: NonEmptySet[Int]): Option[NonEmptyList[TokenMutator]] =
    mutationLevels.toNonEmptyList.map(atLevel).combineAll
}
