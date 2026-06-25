package weaponregex.model.mutation

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

/** A wrapper class for [[weaponregex.model.mutation.TokenMutator]] for exporting to JavaScript
  * @param tokenMutator
  *   The token mutator to be wrapped
  * @note
  *   For JavaScript use only
  */
final class TokenMutatorJS(val tokenMutator: TokenMutator) extends js.Object {

  /** The name of the mutator
    */
  val name: String = tokenMutator.name

  /** The mutation levels that the token mutator falls under
    */
  val levels: js.Array[Int] = tokenMutator.levels.toSortedSet.toJSArray

}
