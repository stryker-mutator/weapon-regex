package weaponregex.mutator

import weaponregex.parser.Parser
import TreeMutator._
import weaponregex.model.mutation.Mutant

class GroupMutatorTest extends munit.FunSuite {
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
}
