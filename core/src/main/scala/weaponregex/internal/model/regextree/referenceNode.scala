package weaponregex.internal.model.regextree

import weaponregex.model.Location

/** Reference to a named capturing group leaf node
  *
  * @param name
  *   The name of the capturing group being referenced
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class NameReference(name: String, override val location: Location) extends Leaf(name, location, """\k<""", ">")

/** Reference to a numbered capturing group leaf node
  * @param num
  *   The order number of the capturing group being referenced
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class NumberReference(num: Int, override val location: Location) extends Leaf(num, location, """\""")
