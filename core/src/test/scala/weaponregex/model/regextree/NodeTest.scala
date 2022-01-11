package weaponregex.model.regextree

import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.model.*

class NodeTest extends munit.FunSuite {
  val locStub: Location = Location(0, 0)(0, 1)
  val leafStubA: Character = Character('A', locStub)
  val leafStubB: Character = Character('B', locStub)
  val leafStubC: Character = Character('C', locStub)

  test("CharacterClass build") {
    val node1 = CharacterClass(Seq(leafStubA, leafStubB, leafStubC), locStub)
    assertEquals(node1.build, "[ABC]")

    val node2 = CharacterClass(Seq(leafStubA, leafStubB, leafStubC), locStub, isPositive = false)
    assertEquals(node2.build, "[^ABC]")
  }

  test("CharacterClassNaked build") {
    val node1 = CharacterClassNaked(Seq(leafStubA, leafStubB, leafStubC), locStub)
    assertEquals(node1.build, "ABC")
  }

  test("CharClassIntersection build") {
    val node1 = CharClassIntersection(Seq(leafStubA, leafStubB, leafStubC), locStub)
    assertEquals(node1.build, "A&&B&&C")
  }

  test("Range build") {
    val node1 = Range(leafStubA, leafStubC, locStub)
    assertEquals(node1.build, "A-C")
  }

  test("Group build") {
    val node1 = Group(leafStubA, isCapturing = true, locStub)
    assertEquals(node1.build, "(A)")

    val node2 = Group(leafStubA, isCapturing = false, locStub)
    assertEquals(node2.build, "(?:A)")
  }

  test("NamedGroup build") {
    val node1 = NamedGroup(leafStubA, "name", locStub)
    assertEquals(node1.build, "(?<name>A)")
  }

  test("FlagNCGroup build") {
    val charSeq = Seq(leafStubA, leafStubB, leafStubC)
    val node1 =
      FlagNCGroup(
        FlagToggle(Flags(charSeq, locStub), hasDash = true, Flags(charSeq, locStub), locStub),
        leafStubA,
        locStub
      )
    assertEquals(node1.build, "(?ABC-ABC:A)")

    val node2 =
      FlagNCGroup(
        FlagToggle(Flags(Nil, locStub), hasDash = true, Flags(charSeq, locStub), locStub),
        leafStubA,
        locStub
      )
    assertEquals(node2.build, "(?-ABC:A)")

    val node3 =
      FlagNCGroup(
        FlagToggle(Flags(charSeq, locStub), hasDash = false, Flags(Nil, locStub), locStub),
        leafStubA,
        locStub
      )
    assertEquals(node3.build, "(?ABC:A)")
  }

  test("FlagGroup build") {
    val charSeq = Seq(leafStubA, leafStubB, leafStubC)
    val node1 =
      FlagToggleGroup(FlagToggle(Flags(charSeq, locStub), hasDash = true, Flags(charSeq, locStub), locStub), locStub)
    assertEquals(node1.build, "(?ABC-ABC)")

    val node2 =
      FlagToggleGroup(FlagToggle(Flags(Nil, locStub), hasDash = true, Flags(charSeq, locStub), locStub), locStub)
    assertEquals(node2.build, "(?-ABC)")

    val node3 =
      FlagToggleGroup(FlagToggle(Flags(charSeq, locStub), hasDash = false, Flags(Nil, locStub), locStub), locStub)
    assertEquals(node3.build, "(?ABC)")
  }

  test("Lookaround build") {
    val node1 = Lookaround(leafStubA, isPositive = false, isLookahead = false, locStub)
    assertEquals(node1.build, "(?<!A)")

    val node2 = Lookaround(leafStubA, isPositive = true, isLookahead = false, locStub)
    assertEquals(node2.build, "(?<=A)")

    val node3 = Lookaround(leafStubA, isPositive = false, isLookahead = true, locStub)
    assertEquals(node3.build, "(?!A)")

    val node4 = Lookaround(leafStubA, isPositive = true, isLookahead = true, locStub)
    assertEquals(node4.build, "(?=A)")
  }

  test("AtomicGroup build") {
    val node1 = AtomicGroup(leafStubA, locStub)
    assertEquals(node1.build, "(?>A)")
  }

  test("Quantifier build") {
    val node1 = Quantifier(leafStubA, 1, locStub, GreedyQuantifier)
    assertEquals(node1.build, "A{1}")

    val node2 = Quantifier(leafStubA, 1, locStub, ReluctantQuantifier)
    assertEquals(node2.build, "A{1}?")

    val node3 = Quantifier(leafStubA, 1, locStub, PossessiveQuantifier)
    assertEquals(node3.build, "A{1}+")

    val node4 = Quantifier(leafStubA, 1, 3, locStub, GreedyQuantifier)
    assertEquals(node4.build, "A{1,3}")

    val node5 = Quantifier(leafStubA, 1, 3, locStub, ReluctantQuantifier)
    assertEquals(node5.build, "A{1,3}?")

    val node6 = Quantifier(leafStubA, 1, 3, locStub, PossessiveQuantifier)
    assertEquals(node6.build, "A{1,3}+")

    val node7 = Quantifier(leafStubA, 1, -1, locStub, GreedyQuantifier)
    assertEquals(node7.build, "A{1,}")

    val node8 = Quantifier(leafStubA, 1, -1, locStub, ReluctantQuantifier)
    assertEquals(node8.build, "A{1,}?")

    val node9 = Quantifier(leafStubA, 1, -1, locStub, PossessiveQuantifier)
    assertEquals(node9.build, "A{1,}+")
  }

  test("ZeroOrOne build") {
    val node1 = ZeroOrOne(leafStubA, locStub, GreedyQuantifier)
    assertEquals(node1.build, "A?")

    val node2 = ZeroOrOne(leafStubA, locStub, ReluctantQuantifier)
    assertEquals(node2.build, "A??")

    val node3 = ZeroOrOne(leafStubA, locStub, PossessiveQuantifier)
    assertEquals(node3.build, "A?+")
  }

  test("ZeroOrMore build") {
    val node1 = ZeroOrMore(leafStubA, locStub, GreedyQuantifier)
    assertEquals(node1.build, "A*")

    val node2 = ZeroOrMore(leafStubA, locStub, ReluctantQuantifier)
    assertEquals(node2.build, "A*?")

    val node3 = ZeroOrMore(leafStubA, locStub, PossessiveQuantifier)
    assertEquals(node3.build, "A*+")
  }

  test("OneOrMore build") {
    val node1 = OneOrMore(leafStubA, locStub, GreedyQuantifier)
    assertEquals(node1.build, "A+")

    val node2 = OneOrMore(leafStubA, locStub, ReluctantQuantifier)
    assertEquals(node2.build, "A+?")

    val node3 = OneOrMore(leafStubA, locStub, PossessiveQuantifier)
    assertEquals(node3.build, "A++")
  }

  test("Concat build") {
    val node1 = Concat(Seq(leafStubA, leafStubB), locStub)
    assertEquals(node1.build, "AB")

    val node2 = Concat(Seq(leafStubA, leafStubB, leafStubC), locStub)
    assertEquals(node2.build, "ABC")
  }

  test("Or build") {
    val node1 = Or(Seq(leafStubA, leafStubB), locStub)
    assertEquals(node1.build, "A|B")

    val node2 = Or(Seq(leafStubA, leafStubB, leafStubC), locStub)
    assertEquals(node2.build, "A|B|C")
  }
}
