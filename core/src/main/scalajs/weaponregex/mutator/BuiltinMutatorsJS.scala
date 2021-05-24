package weaponregex.mutator

import weaponregex.model.mutation.{TokenMutator, TokenMutatorJS}

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation._

/** A wrapper class for [[weaponregex.mutator.BuiltinMutators]] for exporting to JavaScript
  */
@JSExportTopLevel("BuiltinMutators")
@JSExportAll
object BuiltinMutatorsJS {
  private def toTokenMutatorJSArray(mutators: Seq[TokenMutator]): js.Array[TokenMutatorJS] =
    (mutators map TokenMutatorJS).toJSArray

  /** Array of all built-in token mutators
    */
  val all: js.Array[TokenMutatorJS] = toTokenMutatorJSArray(BuiltinMutators.all)

  /** Map from mutation level number to token mutators in that level
    */
  lazy val asMap: js.Map[Int, js.Array[TokenMutatorJS]] =
    (BuiltinMutators.asMap map { case (level, mutators) =>
      level -> toTokenMutatorJSArray(mutators)
    }).toJSMap

  /** Dictionary from mutation level number as a string to token mutators in that level
    */
  lazy val asDict: js.Dictionary[js.Array[TokenMutatorJS]] =
    (BuiltinMutators.asMap map { case (level, mutators) =>
      level.toString -> toTokenMutatorJSArray(mutators)
    }).toJSDictionary

  /** Get all the token mutators in the given mutation level
    * @param mutationLevel Mutation level number
    * @return Array of all the tokens mutators in that level, if any
    */
  def atLevel(mutationLevel: Int): js.Array[TokenMutatorJS] =
    toTokenMutatorJSArray(BuiltinMutators.atLevel(mutationLevel))

  /** Get all the token mutators in the given mutation levels
    * @param mutationLevels Mutation level numbers
    * @return Array of all the tokens mutators in that levels, if any
    */
  def atLevels(mutationLevels: js.Array[Int]): js.Array[TokenMutatorJS] =
    toTokenMutatorJSArray(BuiltinMutators.atLevels(mutationLevels))
}
