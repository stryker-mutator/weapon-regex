package weaponregex.model.regextree

import weaponregex.model._

class LeafTest extends munit.FunSuite {
  var locStub: Location = Location(Position(0, 0), Position(0, 1))

  test("Character build a") {
    val node1 = Character('a', locStub)
    assertEquals(node1.build, "a")
  }

  test("Character build b") {
    val node2 = Character('b', locStub)
    assertEquals(node2.build, "b")
  }

  test("Any dot build") {
    val node1 = AnyDot(locStub)
    assertEquals(node1.build, ".")
  }

  test("MetaChar build a") {
    val node1 = MetaChar("a", locStub)
    assertEquals(node1.build, """\a""")
  }

  test("MetaChar build 0123") {
    val node2 = MetaChar("0123", locStub)
    assertEquals(node2.build, """\0123""")
  }

  test("PredefinedCharClass build") {
    val node1 = PredefinedCharClass("w", locStub)
    assertEquals(node1.build, """\w""")
  }

  test("PredefinedCharClass build negated") {
    val node2 = PredefinedCharClass("W", locStub)
    assertEquals(node2.build, """\W""")
  }

  test("BOL build") {
    val node1 = BOL(locStub)
    assertEquals(node1.build, "^")
  }

  test("EOL build") {
    val node1 = EOL(locStub)
    assertEquals(node1.build, "$")
  }

  test("Boundary build") {
    val node1 = Boundary("G", locStub)
    assertEquals(node1.build, """\G""")
  }

  test("NameReference build") {
    val node1 = NameReference("name", locStub)
    assertEquals(node1.build, """\k<name>""")
  }

  test("NumberReference build") {
    val node1 = NumberReference(4, locStub)
    assertEquals(node1.build, """\4""")
  }

  test("QuoteChar build") {
    val node1 = QuoteChar('y', locStub)
    assertEquals(node1.build, """\y""")
  }

  test("QuoteChar build") {
    val node1 = QuoteChar('y', locStub)
    assertEquals(node1.build, """\y""")
  }

  test("Quote build with end") {
    val node1 = Quote("some quote", hasEnd = false, locStub)
    assertEquals(node1.build, """\Qsome quote""")
  }

  test("Quote build without end") {
    val node2 = Quote("some quote", hasEnd = true, locStub)
    assertEquals(node2.build, """\Qsome quote\E""")
  }

  test("RegexTree build") {
    val pattern: String = """^\w+@[a-zA-Z_]+?\.[a-zA-Z]{2,3}$"""
    val loc = Location(Position(0, 0), Position(0, 1))
    val tree: RegexTree = Concat(
      Seq(
        BOL(loc),
        OneOrMore(
          PredefinedCharClass("w", loc),
          loc,
          GreedyQuantifier
        ),
        Character('@', loc),
        OneOrMore(
          CharacterClass(
            Seq(
              Range(Character('a', loc), Character('z', loc), loc),
              Range(Character('A', loc), Character('Z', loc), loc),
              Character('_', loc)
            ),
            loc
          ),
          loc,
          ReluctantQuantifier
        ),
        QuoteChar('.', loc),
        Quantifier(
          CharacterClass(
            Seq(
              Range(Character('a', loc), Character('z', loc), loc),
              Range(Character('A', loc), Character('Z', loc), loc)
            ),
            loc
          ),
          min = 2,
          max = 3,
          loc,
          GreedyQuantifier
        ),
        EOL(loc)
      ),
      loc
    )
    val buildResult = tree.build
    assertEquals(buildResult, pattern)
  }
}
