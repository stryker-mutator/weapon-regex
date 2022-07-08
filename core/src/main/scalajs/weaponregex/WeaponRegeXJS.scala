package weaponregex

import weaponregex.extension.RegexTreeExtension.RegexTreeMutator
import weaponregex.model.mutation.*
import weaponregex.mutator.BuiltinMutators
import weaponregex.parser.{Parser, ParserFlavor, ParserFlavorJS}

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.annotation.*
import scala.util.{Failure, Success}

/** The API facade of Weapon regeX for JavaScript
  * @note
  *   For JavaScript use only
  */
object WeaponRegeXJS {
  class MutationOptions(
      val mutators: js.Array[TokenMutatorJS] = null,
      val mutationLevels: js.Array[Int] = null,
      val flavor: ParserFlavor = ParserFlavorJS
  ) extends js.Object

  /** Mutate using the given mutators at some specific mutation levels
    * @param pattern
    *   Input regex string
    * @param flags
    *   Regex flags or `undefined`
    * @param options
    *   JavaScript object for Mutation options
    *   {{{
    * {
    *   mutators: [Mutators to be used for mutation],
    *   mutationLevels: [Target mutation levels. If this is `null`, the `mutators` will not be filtered],
    * }
    *   }}}
    * @return
    *   A JavaScript Array of [[weaponregex.model.mutation.Mutant]] if can be parsed, or throw an exception otherwise
    */
  @JSExportTopLevel("mutate")
  def mutate(pattern: String, flags: js.UndefOr[String], options: MutationOptions): js.Array[MutantJS] = {
    val mutators: Seq[TokenMutator] =
      if (options.hasOwnProperty("mutators") && options.mutators != null)
        options.mutators.toSeq map (_.tokenMutator)
      else BuiltinMutators.all

    val mutationLevels: Seq[Int] =
      if (options.hasOwnProperty("mutationLevels") && options.mutationLevels != null)
        options.mutationLevels.toSeq
      else null

    val flavor: ParserFlavor =
      if (options.hasOwnProperty("flavor") && options.flavor != null) options.flavor
      else ParserFlavorJS

    Parser(pattern, flags.toOption, flavor) match {
      case Success(tree)                 => (tree.mutate(mutators, mutationLevels) map MutantJS).toJSArray
      case Failure(throwable: Throwable) => throw throwable
    }
  }

  /** Mutate using the given mutators at some specific mutation levels
    * @param pattern
    *   Input regex string
    * @param options
    *   JavaScript object for Mutation options
    *   {{{
    * {
    *   mutators: [Mutators to be used for mutation],
    *   mutationLevels: [Target mutation levels. If this is `null`, the `mutators` will not be filtered],
    * }
    *   }}}
    * @return
    *   A JavaScript Array of [[weaponregex.model.mutation.Mutant]] if can be parsed, or throw an exception otherwise
    */
  @deprecated("Use `mutate(pattern, flags, options)` instead. This will be removed in the future.", "0.7.x")
  @JSExportTopLevel("mutate")
  def mutate(pattern: String, options: MutationOptions = new MutationOptions): js.Array[MutantJS] =
    mutate(pattern, js.undefined, options)
}
