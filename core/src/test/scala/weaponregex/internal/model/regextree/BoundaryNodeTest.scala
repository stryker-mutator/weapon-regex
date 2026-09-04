package weaponregex.internal.model.regextree

import weaponregex.internal.constant.RegexTreeStubs.LOCATION

class BoundaryNodeTest extends munit.FunSuite {
  test("BOL build") {
    val node1 = BOL(LOCATION)
    assertEquals(node1.build, "^")
  }

  test("EOL build") {
    val node1 = EOL(LOCATION)
    assertEquals(node1.build, "$")
  }

  test("Boundary build") {
    val node1 = Boundary('G', LOCATION)
    assertEquals(node1.build, """\G""")
  }
}
