package weaponregex.constant

import weaponregex.model.Location
import weaponregex.model.regextree.Character

object RegexTreeStubs {
  val LOCATION: Location = Location(0, 0)(0, 1)
  val LEAF_A: Character = Character('A', LOCATION)
  val LEAF_B: Character = Character('B', LOCATION)
  val LEAF_C: Character = Character('C', LOCATION)
}
