package weaponregex.model.mutation

import weaponregex.model.regextree.RegexTree

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.annotation.JSExport

/** A wrapper class for [[weaponregex.model.mutation.TokenMutator]] for exporting to JavaScript
  * @param tokenMutator
  *   The token mutator to be wrapped
  * @note
  *   For JavaScript use only
  */
case class TokenMutatorJS(tokenMutator: TokenMutator) {

  /** The name of the mutator
    */
  @JSExport
  val name: String = tokenMutator.name

  /** The mutation levels that the token mutator falls under
    */
  @JSExport
  val levels: js.Array[Int] = tokenMutator.levels.toJSArray

  /** Apply mutation to the given token
    * @param token
    *   Target token
    * @return
    *   Sequence [[weaponregex.model.mutation.MutantJS]]
    */
  final def apply(token: RegexTree): Seq[MutantJS] = mutate(token)

  /** Mutate the given token
    * @param token
    *   Target token
    * @return
    *   Sequence of [[weaponregex.model.mutation.MutantJS]]
    */
  def mutate(token: RegexTree): Seq[MutantJS] = tokenMutator.mutate(token) map MutantJS
}
