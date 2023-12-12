package weaponregex.internal.model.regextree

import weaponregex.model.Location

/** Beginning of Line (BOL) leaf node
  *
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class BOL(override val location: Location) extends Leaf('^', location)

/** End of Line (EOL) leaf node
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class EOL(override val location: Location) extends Leaf('$', location)

/** Boundary meta character leaf node
  * @param boundary
  *   The literal boundary character without the `\`
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Boundary(boundary: String, override val location: Location) extends Leaf(boundary, location, """\""")
