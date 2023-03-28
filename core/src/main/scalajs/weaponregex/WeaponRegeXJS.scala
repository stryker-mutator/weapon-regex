package weaponregex

import weaponregex.extension.MutationOptionsExtension.MutationOptionsConverter
import weaponregex.extension.RegexTreeExtension.RegexTreeMutator
import weaponregex.model.MutationOptions
import weaponregex.model.mutation.*
import weaponregex.parser.Parser

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.annotation.*

/** The API facade of Weapon regeX for JavaScript
  * @note
  *   For JavaScript use only
  */
object WeaponRegeXJS {

  /** Mutate a regex pattern and flags with the given options.
    *
    * @param pattern
    *   Input regex string
    * @param flags
    *   Regex flags or `undefined`
    * @param options
    *   JavaScript object for Mutation options
    *   {{{
    * {
    *   mutators: [Mutators to be used for mutation. If this is `null`, all built-in mutators will be used.],
    *   mutationLevels: [Target mutation levels. If this is `null`, the `mutators`, will not be filtered.],
    *   flavor: [Regex flavor. By the default, `ParerFlavorJS` will be used.]
    * }
    *   }}}
    * @return
    *   A JavaScript Array of [[weaponregex.model.mutation.Mutant]] if can be parsed, or throw an exception otherwise
    */
  @JSExportTopLevel("mutate")
  def mutate(pattern: String, flags: js.UndefOr[String], options: MutationOptions): js.Array[MutantJS] = {
    val (mutators, mutationLevels, flavor) = options.toScala
    val flagsOpt = flags.toOption.filterNot(_ == null).filterNot(_.isEmpty)

    Parser(pattern, flagsOpt, flavor) match {
      case Right(tree) => tree.mutate(mutators, mutationLevels).map(MutantJS(_)).toJSArray
      case Left(msg)   => throw new RuntimeException(msg)
    }
  }

  /** Mutate a regex pattern with the given options.
    *
    * @param pattern
    *   Input regex string
    * @param options
    *   JavaScript object for Mutation options
    *   {{{
    * {
    *   mutators: [Mutators to be used for mutation. If this is `null`, all built-in mutators will be used.],
    *   mutationLevels: [Target mutation levels. If this is `null`, the `mutators`, will not be filtered.],
    *   flavor: [Regex flavor. By the default, `ParerFlavorJS` will be used.]
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
