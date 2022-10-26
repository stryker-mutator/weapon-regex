package weaponregex.model.regextree

import weaponregex.constant.RegexTreeStubs.*
import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder

class CapturingNodeTest extends munit.FunSuite {
  test("Group build") {
    val node1 = Group(LEAF_A, isCapturing = true, LOCATION)
    assertEquals(node1.build, "(A)")

    val node2 = Group(LEAF_A, isCapturing = false, LOCATION)
    assertEquals(node2.build, "(?:A)")
  }

  test("NamedGroup build") {
    val node1 = NamedGroup(LEAF_A, "name", LOCATION)
    assertEquals(node1.build, "(?<name>A)")
  }

  test("FlagNCGroup build") {
    val charSeq = Seq(LEAF_A, LEAF_B, LEAF_C)
    val node1 =
      FlagNCGroup(
        FlagToggle(Flags(charSeq, LOCATION), hasDash = true, Flags(charSeq, LOCATION), LOCATION),
        LEAF_A,
        LOCATION
      )
    assertEquals(node1.build, "(?ABC-ABC:A)")

    val node2 =
      FlagNCGroup(
        FlagToggle(Flags(Nil, LOCATION), hasDash = true, Flags(charSeq, LOCATION), LOCATION),
        LEAF_A,
        LOCATION
      )
    assertEquals(node2.build, "(?-ABC:A)")

    val node3 =
      FlagNCGroup(
        FlagToggle(Flags(charSeq, LOCATION), hasDash = false, Flags(Nil, LOCATION), LOCATION),
        LEAF_A,
        LOCATION
      )
    assertEquals(node3.build, "(?ABC:A)")
  }

  test("FlagGroup build") {
    val charSeq = Seq(LEAF_A, LEAF_B, LEAF_C)
    val node1 =
      FlagToggleGroup(
        FlagToggle(Flags(charSeq, LOCATION), hasDash = true, Flags(charSeq, LOCATION), LOCATION),
        LOCATION
      )
    assertEquals(node1.build, "(?ABC-ABC)")

    val node2 =
      FlagToggleGroup(FlagToggle(Flags(Nil, LOCATION), hasDash = true, Flags(charSeq, LOCATION), LOCATION), LOCATION)
    assertEquals(node2.build, "(?-ABC)")

    val node3 =
      FlagToggleGroup(FlagToggle(Flags(charSeq, LOCATION), hasDash = false, Flags(Nil, LOCATION), LOCATION), LOCATION)
    assertEquals(node3.build, "(?ABC)")
  }

  test("Lookaround build") {
    val node1 = Lookaround(LEAF_A, isPositive = false, isLookahead = false, LOCATION)
    assertEquals(node1.build, "(?<!A)")

    val node2 = Lookaround(LEAF_A, isPositive = true, isLookahead = false, LOCATION)
    assertEquals(node2.build, "(?<=A)")

    val node3 = Lookaround(LEAF_A, isPositive = false, isLookahead = true, LOCATION)
    assertEquals(node3.build, "(?!A)")

    val node4 = Lookaround(LEAF_A, isPositive = true, isLookahead = true, LOCATION)
    assertEquals(node4.build, "(?=A)")
  }

  test("AtomicGroup build") {
    val node1 = AtomicGroup(LEAF_A, LOCATION)
    assertEquals(node1.build, "(?>A)")
  }
}
