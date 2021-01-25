package weaponregex.model.mutation

import weaponregex.model.regextree.RegexTree

trait TokenMutator {

  /** The name of the mutator
    */
  val name: String

  /** The mutation levels that the token mutator falls under
    */
  val levels: Seq[Int]

  /** A short description  of the mutator
    */
  val description: String = name

  /** Apply mutation to the given token
    * @param token Target token
    * @return Sequence of strings, which are mutations of the original token
    */
  final def apply(token: RegexTree): Seq[String] = mutate(token)

  /** Mutate the given token
    * @param token Target token
    * @return Sequence of strings, which are mutations of the original token
    */
  def mutate(token: RegexTree): Seq[String]
}
