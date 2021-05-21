package weaponregex.mutator

import weaponregex.parser.Parser
import weaponregex.`extension`.RegexTreeExtension.RegexTreeMutator

class PredefCharClassMutatorTest extends munit.FunSuite {
  test("Negates Predefined Character Class") {
    val pattern = """\w\W\d\D\s\S"""
    val parsedTree = Parser(pattern).get

    val mutants: Seq[String] = parsedTree.mutate(Seq(PredefCharClassNegation)) map (_.pattern)

    val expected: Seq[String] = Seq(
      """\W\W\d\D\s\S""",
      """\w\w\d\D\s\S""",
      """\w\W\D\D\s\S""",
      """\w\W\d\d\s\S""",
      """\w\W\d\D\S\S""",
      """\w\W\d\D\s\s"""
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Does not mutate (negate) similar characters") {
    val pattern = "wWdDsS"
    val parsedTree = Parser(pattern).get

    val mutants: Seq[String] = parsedTree.mutate(Seq(PredefCharClassNegation)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }

  test("Nullifies Predefined Character Class") {
    val pattern = """\w\W\d\D\s\S"""
    val parsedTree = Parser(pattern).get

    val mutants: Seq[String] = parsedTree.mutate(Seq(PredefCharClassNullification)) map (_.pattern)

    val expected: Seq[String] = Seq(
      """w\W\d\D\s\S""",
      """\wW\d\D\s\S""",
      """\w\Wd\D\s\S""",
      """\w\W\dD\s\S""",
      """\w\W\d\Ds\S""",
      """\w\W\d\D\sS"""
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Does not mutate (nullify) similar characters") {
    val pattern = "wWdDsS"
    val parsedTree = Parser(pattern).get

    val mutants: Seq[String] = parsedTree.mutate(Seq(PredefCharClassNullification)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }

  test("Changes Predefined Character Class to Any Char") {
    val pattern = """\w\W\d\D\s\S"""
    val parsedTree = Parser(pattern).get

    val mutants: Seq[String] = parsedTree.mutate(Seq(PredefCharClassAnyChar)) map (_.pattern)

    val expected: Seq[String] = Seq(
      """[\w\W]\W\d\D\s\S""",
      """\w[\W\w]\d\D\s\S""",
      """\w\W[\d\D]\D\s\S""",
      """\w\W\d[\D\d]\s\S""",
      """\w\W\d\D[\s\S]\S""",
      """\w\W\d\D\s[\S\s]"""
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Does not mutate (change) similar characters") {
    val pattern = "wWdDsS"
    val parsedTree = Parser(pattern).get

    val mutants: Seq[String] = parsedTree.mutate(Seq(PredefCharClassAnyChar)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }

  test("Negates POSIX Character Class") {
    val pattern = """\p{Alpha}\P{Alpha}"""
    val parsedTree = Parser(pattern).get

    val mutants: Seq[String] = parsedTree.mutate(Seq(POSIXCharClassNegation)) map (_.pattern)

    val expected: Seq[String] = Seq(
      """\P{Alpha}\P{Alpha}""",
      """\p{Alpha}\p{Alpha}"""
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }
}
