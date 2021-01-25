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

  /** The [[weaponregex.model.Location]] of the node in the regex string
    */
  val location: Location

  /** Build the tree into a String
    */
  lazy val build: String = buildWhile(_ => true)

  /** Build the tree into a String with a child replaced by a string.
    * @param child Child to be replaced
    * @param childString Replacement String
    * @return A String representation of the tree
    */
  def buildWith(child: RegexTree, childString: String): String

  /** Build the tree into a String while a predicate holds for a given child.
    * @param pred Predicate on a child
    * @return A String representation of the tree
    */
  def buildWhile(pred: RegexTree => Boolean): String
}
