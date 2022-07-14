package weaponregex

import weaponregex.extension.RegexTreeExtension.RegexTreeMutator
import weaponregex.model.mutation.*
import weaponregex.mutator.BuiltinMutators
import weaponregex.parser.{Parser, ParserFlavor, ParserFlavorJVM}

/** The API facade of Weapon regeX for Scala/JVM
  */
object WeaponRegeX {

  /** Mutate using the given mutators in some specific mutation levels
    * @param pattern
    *   Input regex string
    * @param mutators
    *   Mutators to be used for mutation
    * @param mutationLevels
    *   Target mutation levels. If this is `null`, the `mutators` will not be filtered.
    * @return
    *   A `Right` of a sequence of [[weaponregex.model.mutation.Mutant]] if can be parsed, a `Left` with the error message otherwise
    */
  def mutate(
      pattern: String,
      mutators: Seq[TokenMutator] = BuiltinMutators.all,
      mutationLevels: Seq[Int] = null,
      flavor: ParserFlavor = ParserFlavorJVM
  ): Either[String, Seq[Mutant]] = Parser(pattern, flavor) map (_.mutate(mutators, mutationLevels))
}
