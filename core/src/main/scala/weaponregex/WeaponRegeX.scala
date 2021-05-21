package weaponregex

import weaponregex.parser.Parser
import weaponregex.model.mutation._
import weaponregex.mutator.BuiltinMutators
import weaponregex.parser.{ParserFlavor, ParserFlavorJVM}
import weaponregex.`extension`.RegexTreeExtension.RegexTreeMutator

import scala.scalajs.js.annotation._
import scala.util.Try

/** The API facade of Weapon regeX for Scala/JVM
  */
@JSExportTopLevel("WeaponRegeX")
@JSExportAll
object WeaponRegeX {

  /** Mutate using the given mutators in some specific mutation levels
    * @param pattern Input regex string
    * @param mutators Mutators to be used for mutation
    * @param mutationLevels Target mutation levels. If this is `null`, the `mutators` will not be filtered.
    * @return A `Success` of a sequence of [[weaponregex.model.mutation.Mutant]] if can be parsed, a `Failure` otherwise
    */
  def mutate(
      pattern: String,
      mutators: Seq[TokenMutator] = BuiltinMutators.all,
      mutationLevels: Seq[Int] = null,
      flavor: ParserFlavor = ParserFlavorJVM
  ): Try[Seq[Mutant]] = Parser(pattern, flavor) map (_.mutate(mutators, mutationLevels))
}
