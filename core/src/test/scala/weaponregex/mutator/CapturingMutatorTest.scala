package weaponregex.mutator

import weaponregex.parser.Parser
import TreeMutator._
import weaponregex.model.mutation.Mutant

class CapturingMutatorTest extends munit.FunSuite {
  test("Changes capturing group to non-capturing group") {
    val pattern = "(hello)"
    val parsedTree = Parser(pattern).get

    val mutants: Seq[Mutant] = parsedTree.mutate(Seq(GroupToNCGroup))

    val expected: Seq[String] = Seq("(?:hello)")

    assertEquals(clue(mutants).length, expected.length)
    assertEquals(clue(mutants) map (_.pattern), expected)
  }

  test("Does not change escaped capturing groups") {
    val pattern = "\\(hello\\)"
    val parsedTree = Parser(pattern).get

    val mutants: Seq[Mutant] = parsedTree.mutate(Seq(GroupToNCGroup))

    assertEquals(clue(mutants), Nil)
  }

  test("Negates lookaround constructs") {
    val pattern = "(?=abc)(?!abc)(?<=abc)(?<!abc)"
    val parsedTree = Parser(pattern).get

    val mutants: Seq[String] = parsedTree.mutate(Seq(LookaroundNegation)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "(?!abc)(?!abc)(?<=abc)(?<!abc)",
      "(?=abc)(?=abc)(?<=abc)(?<!abc)",
      "(?=abc)(?!abc)(?<!abc)(?<!abc)",
      "(?=abc)(?!abc)(?<=abc)(?<=abc)"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }
}
