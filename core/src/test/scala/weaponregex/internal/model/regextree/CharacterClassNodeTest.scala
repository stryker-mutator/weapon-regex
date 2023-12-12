package weaponregex.internal.model.regextree

import weaponregex.internal.constant.RegexTreeStubs.*
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeStringBuilder

class CharacterClassNodeTest extends munit.FunSuite {
  test("CharacterClass build") {
    val node1 = CharacterClass(Seq(LEAF_A, LEAF_B, LEAF_C), LOCATION)
    assertEquals(node1.build, "[ABC]")

    val node2 = CharacterClass(Seq(LEAF_A, LEAF_B, LEAF_C), LOCATION, isPositive = false)
    assertEquals(node2.build, "[^ABC]")
  }

  test("CharacterClassNaked build") {
    val node1 = CharacterClassNaked(Seq(LEAF_A, LEAF_B, LEAF_C), LOCATION)
    assertEquals(node1.build, "ABC")
  }

  test("CharClassIntersection build") {
    val node1 = CharClassIntersection(Seq(LEAF_A, LEAF_B, LEAF_C), LOCATION)
    assertEquals(node1.build, "A&&B&&C")
  }

  test("Range build") {
    val node1 = Range(LEAF_A, LEAF_C, LOCATION)
    assertEquals(node1.build, "A-C")
  }
}
