package weaponregex.model.regextree

import weaponregex.constant.RegexTreeStubs.LOCATION
import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder

class PredefinedCharClassNodeTest extends munit.FunSuite {
  test("PredefinedCharClass build") {
    val node1 = PredefinedCharClass("w", LOCATION)
    assertEquals(node1.build, """\w""")
  }

  test("PredefinedCharClass build negated") {
    val node2 = PredefinedCharClass("W", LOCATION)
    assertEquals(node2.build, """\W""")
  }

  test("POSIXCharClass build") {
    val node1 = POSIXCharClass("hello_World_0123", LOCATION)
    assertEquals(node1.build, """\p{hello_World_0123}""")
  }

  test("POSIXCharClass build negated") {
    val node1 = POSIXCharClass("hello_World_0123", LOCATION, isPositive = false)
    assertEquals(node1.build, """\P{hello_World_0123}""")
  }
}
