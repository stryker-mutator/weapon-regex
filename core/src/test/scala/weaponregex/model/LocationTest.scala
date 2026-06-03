package weaponregex.model

import cats.kernel.laws.discipline.OrderTests
import munit.DisciplineSuite
import weaponregex.arbitraries.*

class LocationTest extends DisciplineSuite {

  checkAll("Location.OrderTests", OrderTests[Location].order)

  test("Location start must be <= end") {
    interceptMessage[IllegalArgumentException]("requirement failed: Location end 0:0 must be >= start 1:0") {
      Location(Position(1, 0), Position(0, 0))
    }
  }

  test("Location show should be in the format [start, end)") {
    val location = Location(Position(1, 2), Position(3, 4))
    assertEquals(location.show, "[1:2, 3:4)")
  }
}
