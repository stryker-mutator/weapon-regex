package weaponregex.model.mutation

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.annotation.*

/** A wrapper class for [[weaponregex.model.mutation.Mutant]] for exporting to JavaScript
  * @param mutant
  *   The mutant to be wrapped
  * @note
  *   For JavaScript use only
  */
@JSExportAll
case class MutantJS(mutant: Mutant) {

  /** The replacement pattern
    */
  val pattern: String = mutant.pattern

  /** Name of the mutation
    */
  val name: String = mutant.name

  /** [[mutationtesting.Location]] in the original string where the mutation occurred
    */
  val location: js.Object = js.Dynamic.literal(
    start = js.Dynamic.literal(
      line = mutant.location.start.line,
      column = mutant.location.start.column
    ),
    end = js.Dynamic.literal(
      line = mutant.location.end.line,
      column = mutant.location.end.column
    ),
    show = mutant.location.show
  )

  /** The mutation levels of the mutator
    */
  val levels: js.Array[Int] = mutant.levels.toSortedSet.toJSArray

  /** Description on the mutation
    */
  val description: String = mutant.description

  /** The part of the pattern that has been changed
    */
  val replacement: String = mutant.replacement
}
