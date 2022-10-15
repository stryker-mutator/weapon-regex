package weaponregex.model.regextree

import weaponregex.constant.RegexTreeStubs.*
import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder

class LogicalNodeTest extends munit.FunSuite {
  test("Concat build") {
    val node1 = Concat(Seq(LEAF_A, LEAF_B), LOCATION)
    assertEquals(node1.build, "AB")

    val node2 = Concat(Seq(LEAF_A, LEAF_B, LEAF_C), LOCATION)
    assertEquals(node2.build, "ABC")
  }

  test("Or build") {
    val node1 = Or(Seq(LEAF_A, LEAF_B), LOCATION)
    assertEquals(node1.build, "A|B")

    val node2 = Or(Seq(LEAF_A, LEAF_B, LEAF_C), LOCATION)
    assertEquals(node2.build, "A|B|C")
  }
}
