package weaponregex.model

import cats.kernel.laws.discipline.OrderTests
import munit.DisciplineSuite
import weaponregex.arbitraries.*

class PositionTest extends DisciplineSuite {

  checkAll("Position.OrderTests", OrderTests[Position].order)

}
