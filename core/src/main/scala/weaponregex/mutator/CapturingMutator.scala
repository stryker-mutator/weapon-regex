package weaponregex.mutator

import weaponregex.model.mutation.TokenMutator
import weaponregex.model.regextree._

/** Modify capturing group to non-capturing group
  *
  * ''Mutation level(s):'' 2, 3
  * @example `(abc)` ⟶ `(?:abc)`
  */
object GroupToNCGroup extends TokenMutator {
  override val name: String = "Capturing group to non-capturing group"
  override val levels: Seq[Int] = Seq(2, 3)
  override val description: String = "Modify capturing group to non-capturing group"

  override def mutate(token: RegexTree): Seq[String] = (token match {
    case group @ Group(_, true, _) => Seq(group.copy(isCapturing = false))
    case _                         => Nil
  }) map (_.build)
}
