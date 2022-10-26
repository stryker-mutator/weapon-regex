package weaponregex.model.regextree

import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.model.*

class RegexTreeTest extends munit.FunSuite {
  test("RegexTree build") {
    val pattern: String = """^\w+@[a-zA-Z_]+?\.[a-zA-Z]{2,3}$"""
    val loc = Location(0, 0)(0, 1)
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
