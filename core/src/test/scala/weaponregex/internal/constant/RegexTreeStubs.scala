package weaponregex.internal.constant

import mutationtesting.{Location, Position}
import weaponregex.internal.model.regextree.Character

object RegexTreeStubs {
  val LOCATION: Location = Location(Position(0, 0), Position(0, 1))
  val LEAF_A: Character = Character('A', LOCATION)
  val LEAF_B: Character = Character('B', LOCATION)
  val LEAF_C: Character = Character('C', LOCATION)
}
