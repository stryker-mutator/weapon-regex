package weaponregex.internal.model.regextree

import cats.data.NonEmptyList
import weaponregex.internal.constant.RegexTreeStubs.*
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeStringBuilder

class LogicalNodeTest extends munit.FunSuite {
  test("Concat build") {
    val node1 = Concat(NonEmptyList.of(LEAF_A, LEAF_B), LOCATION)
    assertEquals(node1.build, "AB")

    val node2 = Concat(NonEmptyList.of(LEAF_A, LEAF_B, LEAF_C), LOCATION)
    assertEquals(node2.build, "ABC")
  }

  test("Or build") {
    val node1 = Or(NonEmptyList.of(LEAF_A, LEAF_B), LOCATION)
    assertEquals(node1.build, "A|B")

    val node2 = Or(NonEmptyList.of(LEAF_A, LEAF_B, LEAF_C), LOCATION)
    assertEquals(node2.build, "A|B|C")
  }
}
