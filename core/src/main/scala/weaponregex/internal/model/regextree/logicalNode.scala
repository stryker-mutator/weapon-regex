package weaponregex.internal.model.regextree

import cats.data.NonEmptyList
import weaponregex.model.Location

/** Concatenation node
  *
  * @param nodes
  *   The nodes that are being concatenated
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Concat(nodes: NonEmptyList[RegexTree], override val location: Location) extends Node(nodes.toList, location)

/** Or node (e.g. `a|b|c`)
  * @param nodes
  *   The nodes that are being "or-ed"
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Or(nodes: NonEmptyList[RegexTree], override val location: Location)
    extends Node(nodes.toList, location, sep = "|")
