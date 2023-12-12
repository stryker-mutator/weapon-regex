package weaponregex.internal.model.regextree

import weaponregex.internal.constant.RegexTreeStubs.*
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeStringBuilder

class QuantifierNodeTest extends munit.FunSuite {
  test("Quantifier build") {
    val node1 = Quantifier(LEAF_A, 1, LOCATION, GreedyQuantifier)
    assertEquals(node1.build, "A{1}")

    val node2 = Quantifier(LEAF_A, 1, LOCATION, ReluctantQuantifier)
    assertEquals(node2.build, "A{1}?")

    val node3 = Quantifier(LEAF_A, 1, LOCATION, PossessiveQuantifier)
    assertEquals(node3.build, "A{1}+")

    val node4 = Quantifier(LEAF_A, 1, 3, LOCATION, GreedyQuantifier)
    assertEquals(node4.build, "A{1,3}")

    val node5 = Quantifier(LEAF_A, 1, 3, LOCATION, ReluctantQuantifier)
    assertEquals(node5.build, "A{1,3}?")

    val node6 = Quantifier(LEAF_A, 1, 3, LOCATION, PossessiveQuantifier)
    assertEquals(node6.build, "A{1,3}+")

    val node7 = Quantifier(LEAF_A, 1, -1, LOCATION, GreedyQuantifier)
    assertEquals(node7.build, "A{1,}")

    val node8 = Quantifier(LEAF_A, 1, -1, LOCATION, ReluctantQuantifier)
    assertEquals(node8.build, "A{1,}?")

    val node9 = Quantifier(LEAF_A, 1, -1, LOCATION, PossessiveQuantifier)
    assertEquals(node9.build, "A{1,}+")
  }

  test("ZeroOrOne build") {
    val node1 = ZeroOrOne(LEAF_A, LOCATION, GreedyQuantifier)
    assertEquals(node1.build, "A?")

    val node2 = ZeroOrOne(LEAF_A, LOCATION, ReluctantQuantifier)
    assertEquals(node2.build, "A??")

    val node3 = ZeroOrOne(LEAF_A, LOCATION, PossessiveQuantifier)
    assertEquals(node3.build, "A?+")
  }

  test("ZeroOrMore build") {
    val node1 = ZeroOrMore(LEAF_A, LOCATION, GreedyQuantifier)
    assertEquals(node1.build, "A*")

    val node2 = ZeroOrMore(LEAF_A, LOCATION, ReluctantQuantifier)
    assertEquals(node2.build, "A*?")

    val node3 = ZeroOrMore(LEAF_A, LOCATION, PossessiveQuantifier)
    assertEquals(node3.build, "A*+")
  }

  test("OneOrMore build") {
    val node1 = OneOrMore(LEAF_A, LOCATION, GreedyQuantifier)
    assertEquals(node1.build, "A+")

    val node2 = OneOrMore(LEAF_A, LOCATION, ReluctantQuantifier)
    assertEquals(node2.build, "A+?")

    val node3 = OneOrMore(LEAF_A, LOCATION, PossessiveQuantifier)
    assertEquals(node3.build, "A++")
  }
}
