package weaponregex.model.regextree

import weaponregex.model.Location

/** Concatenation node
  *
  * @param nodes
  *   The nodes that are being concatenated
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Concat(nodes: Seq[RegexTree], override val location: Location) extends Node(nodes, location)

/** Or node (e.g. `a|b|c`)
  * @param nodes
  *   The nodes that are being "or-ed"
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Or(nodes: Seq[RegexTree], override val location: Location) extends Node(nodes, location, sep = "|")
