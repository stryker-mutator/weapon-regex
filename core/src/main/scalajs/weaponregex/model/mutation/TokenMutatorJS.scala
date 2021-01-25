package weaponregex.model.mutation

import weaponregex.model.regextree.RegexTree
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.JSConverters._
import scala.scalajs.js

/** A wrapper class for [[weaponregex.model.mutation.TokenMutator]] for exporting to JavaScript
  * @param tokenMutator The token mutator to be wrapped
  * @note For JavaScript use only
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

  /** A short description  of the mutator
    */
  @JSExport
  val description: String = tokenMutator.description

  /** Apply mutation to the given token
    * @param token Target token
    * @return Sequence of strings, which are mutations of the original token
    */
  final def apply(token: RegexTree): Seq[String] = mutate(token)

  /** Mutate the given token
    * @param token Target token
    * @return Sequence of strings, which are mutations of the original token
    */
  def mutate(token: RegexTree): Seq[String] = tokenMutator.mutate(token)
}
