package weaponregex.model.regextree

import weaponregex.model.Location

/** The abstraction of a RegexTree node
  */
trait RegexTree {

  /** The children that fall under this node
    */
  val children: Seq[RegexTree]

  /** The string that is put in front of the node's children when building
    */
  val prefix: String

  /** The string that is put after the node's children when building
    */
  val postfix: String

  /** The string that is put in between the node's children when building
    */
  val sep: String

  /** The [[weaponregex.model.Location]] of the node in the regex string
    */
  val location: Location
}
