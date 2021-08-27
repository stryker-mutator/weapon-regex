package weaponregex.model.mutation

import weaponregex.model.Location

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation._

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

  /** [[weaponregex.model.Location]] in the original string where the mutation occurred
    */
  val location: Location = mutant.location

  /** The mutation levels of the mutator
    */
  val levels: js.Array[Int] = mutant.levels.toJSArray

  /** Description on the mutation
    */
  val description: String = mutant.description
}
