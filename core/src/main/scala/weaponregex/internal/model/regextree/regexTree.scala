package weaponregex.internal.model.regextree

import weaponregex.model.Location

/** The abstraction of a RegexTree node
  */
sealed trait RegexTree {

  /** The string that is put in front of the node's children when building
    */
  def prefix: String

  /** The string that is put after the node's children when building
    */
  def postfix: String

  /** The [[weaponregex.model.Location]] of the node in the regex string
    */
  def location: Location
}

/** The non-terminal node of the [[weaponregex.internal.model.regextree.RegexTree]] that have at least one child node
  * @param children
  *   The children that fall under this node
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  * @param prefix
  *   The string that is put in front of the node's children when building
  * @param postfix
  *   The string that is put after the node's children when building
  * @param sep
  *   The string that is put in between the node's children when building
  */
abstract class Node(
    val children: Seq[RegexTree],
    override val location: Location,
    override val prefix: String = "",
    override val postfix: String = "",
    val sep: String = ""
) extends RegexTree

/** The leaf of the [[weaponregex.internal.model.regextree.RegexTree]] (terminal node) that have no children node
  * @param value
  *   The value that the leaf holds
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  * @param prefix
  *   The string that is put in front of the leaf's value when building
  * @param postfix
  *   The string that is put after the leaf's value when building
  */
abstract class Leaf[A](
    val value: A,
    override val location: Location,
    override val prefix: String = "",
    override val postfix: String = ""
) extends RegexTree
