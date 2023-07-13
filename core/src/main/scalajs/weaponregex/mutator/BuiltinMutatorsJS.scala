package weaponregex.mutator

import weaponregex.model.mutation.{TokenMutator, TokenMutatorJS}

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.annotation.*

/** A wrapper class for [[weaponregex.mutator.BuiltinMutators]] for exporting to JavaScript
  */
@JSExportTopLevel("BuiltinMutators")
@JSExportAll
object BuiltinMutatorsJS {

  /** Convert a sequence of [[weaponregex.model.mutation.TokenMutator]] to a JS array of
    * [[weaponregex.model.mutation.TokenMutatorJS]]
    * @param mutators
    *   The token mutators to be converted
    * @return
    *   A JS array of [[weaponregex.model.mutation.TokenMutatorJS]]
    */
  private def toTokenMutatorJSArray(mutators: Seq[TokenMutator]): js.Array[TokenMutatorJS] =
    mutators.map(TokenMutatorJS(_)).toJSArray

  /** JS Array of all built-in token mutators
    */
  val all: js.Array[TokenMutatorJS] = toTokenMutatorJSArray(BuiltinMutators.all)

  /** JS Map that maps from a token mutator class names to the associating token mutator
    */
  @JSExportTopLevel("mutators")
  val byName: js.Map[String, TokenMutatorJS] =
    (BuiltinMutators.byName transform ((_, mutator) => TokenMutatorJS(mutator))).toJSMap

  /** JS Map that maps from mutation level number to token mutators in that level
    */
  lazy val byLevel: js.Map[Int, js.Array[TokenMutatorJS]] =
    (BuiltinMutators.byLevel transform ((_, mutators) => toTokenMutatorJSArray(mutators))).toJSMap

  /** Get all the token mutators in the given mutation level
    * @param mutationLevel
    *   Mutation level number
    * @return
    *   Array of all the tokens mutators in that level, if any
    */
  def atLevel(mutationLevel: Int): js.Array[TokenMutatorJS] =
    toTokenMutatorJSArray(BuiltinMutators.atLevel(mutationLevel))

  /** Get all the token mutators in the given mutation levels
    * @param mutationLevels
    *   Mutation level numbers
    * @return
    *   Array of all the tokens mutators in that levels, if any
    */
  def atLevels(mutationLevels: js.Array[Int]): js.Array[TokenMutatorJS] =
    toTokenMutatorJSArray(BuiltinMutators.atLevels(mutationLevels.toSeq))
}
