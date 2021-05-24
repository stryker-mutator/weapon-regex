package weaponregex.mutator

import weaponregex.model.mutation.TokenMutator

/** The object that manages all built-in token mutators
  */
object BuiltinMutators {

  /** Sequence of all built-in token mutators
    */
  val all: Seq[TokenMutator] = Seq(
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
    POSIXCharClassNegation,
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

  /** Map from mutation level number to token mutators in that level
    */
  lazy val asMap: Map[Int, Seq[TokenMutator]] =
    all.foldLeft(Map.empty[Int, Seq[TokenMutator]])((levels, mutator) =>
      mutator.levels.foldLeft(levels)((ls, level) => ls + (level -> (ls.getOrElse(level, Nil) :+ mutator)))
    )

  final def apply(mutationLevel: Int): Seq[TokenMutator] = atLevel(mutationLevel)

  final def apply(mutationLevels: Seq[Int]): Seq[TokenMutator] = atLevels(mutationLevels)

  /** Get all the token mutators in the given mutation level
    * @param mutationLevel Mutation level number
    * @return Sequence of all the tokens mutators in that level, if any
    */
  def atLevel(mutationLevel: Int): Seq[TokenMutator] = asMap.getOrElse(mutationLevel, Nil)

  /** Get all the token mutators in the given mutation levels
    * @param mutationLevels Mutation level numbers
    * @return Sequence of all the tokens mutators in that levels, if any
    */
  def atLevels(mutationLevels: Seq[Int]): Seq[TokenMutator] = mutationLevels flatMap atLevel
}
