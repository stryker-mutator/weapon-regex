package weaponregex.internal.model.regextree

import cats.data.NonEmptyList
import weaponregex.internal.constant.RegexTreeStubs.LOCATION
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeStringBuilder

class RegexTreeTest extends munit.FunSuite {
  test("RegexTree build") {
    val pattern: String = """^\w+@[a-zA-Z_]+?\.[a-zA-Z]{2,3}$"""
    val loc = LOCATION
    val tree: RegexTree = Concat(
      NonEmptyList.of(
        BOL(loc),
        OneOrMore(
          PredefinedCharClass('w', loc),
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

  test("RegexTree build is cached per node") {
    val loc = LOCATION
    val child = CharacterClass(Seq(Character('a', loc), Character('b', loc)), loc)
    val tree = OneOrMore(child, loc, GreedyQuantifier)

    assertEquals(tree.build, "[ab]+")
    // The second call reuses the cached string instead of building it again
    assert(tree.build eq tree.build)
    assertEquals(tree.build, "[ab]+")
  }

  test("RegexTree build of an already built child is unaffected") {
    val loc = LOCATION
    val child = CharacterClass(Seq(Character('a', loc), Character('b', loc)), loc)
    // Build the child before its parent, so the parent builds on top of a filled cache
    assertEquals(child.build, "[ab]")

    val tree = OneOrMore(child, loc, GreedyQuantifier)
    assertEquals(tree.build, "[ab]+")
    assertEquals(tree.buildWith(child, "[cd]"), "[cd]+")
    // Replacing a child in one build does not disturb the cached strings
    assertEquals(child.build, "[ab]")
    assertEquals(tree.build, "[ab]+")
  }

  test("FlagToggle builds the dash between its flags") {
    val loc = LOCATION
    val flagToggle = FlagToggle(Flags(Seq(Character('i', loc)), loc), true, Flags(Seq(Character('x', loc)), loc), loc)

    assertEquals(flagToggle.build, "i-x")
    assertEquals(FlagToggleGroup(flagToggle, loc).build, "(?i-x)")
  }
}
