package weaponregex.mutator

import weaponregex.extension.RegexTreeExtension.RegexTreeMutator
import weaponregex.parser.Parser

class QuantifierMutatorTest extends munit.FunSuite {
  test("Removes greedy quantifier") {
    val pattern = "a?b*c+d{1}e{1,}f{1,2}g"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierRemoval)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "ab*c+d{1}e{1,}f{1,2}g",
      "a?bc+d{1}e{1,}f{1,2}g",
      "a?b*cd{1}e{1,}f{1,2}g",
      "a?b*c+de{1,}f{1,2}g",
      "a?b*c+d{1}ef{1,2}g",
      "a?b*c+d{1}e{1,}fg"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Does not remove escaped greedy quantifiers") {
    val pattern = """a\?b\*c\+d\{1\}e\{1,\}f\{1,2\}g"""
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierRemoval)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }

  test("Removes reluctant quantifier") {
    val pattern = "a??b*?c+?d{1}?e{1,}?f{1,2}?g"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierRemoval)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "ab*?c+?d{1}?e{1,}?f{1,2}?g",
      "a??bc+?d{1}?e{1,}?f{1,2}?g",
      "a??b*?cd{1}?e{1,}?f{1,2}?g",
      "a??b*?c+?de{1,}?f{1,2}?g",
      "a??b*?c+?d{1}?ef{1,2}?g",
      "a??b*?c+?d{1}?e{1,}?fg"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Does not remove escaped greedy quantifiers") {
    val pattern = """a\?\?b\*\?c\+\?d\{1\}\?e\{1,\}\?f\{1,2\}\?g"""
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierRemoval)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }

  test("Removes possessive quantifier") {
    val pattern = "a?+b*+c++d{1}+e{1,}+f{1,2}+"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierRemoval)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "ab*+c++d{1}+e{1,}+f{1,2}+",
      "a?+bc++d{1}+e{1,}+f{1,2}+",
      "a?+b*+cd{1}+e{1,}+f{1,2}+",
      "a?+b*+c++de{1,}+f{1,2}+",
      "a?+b*+c++d{1}+ef{1,2}+",
      "a?+b*+c++d{1}+e{1,}+f"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Does not remove escaped possessive quantifiers") {
    val pattern = """a\?\+b\*\+c\+\+d\{1\}\+e\{1,\}\+f\{1,2\}\+g"""
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierRemoval)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }

  test("Changes quantifier {n}") {
    val pattern = "a{1}"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierNChange)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "a{0,1}",
      "a{1,}"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Modifies quantifier {n,}") {
    val pattern = "a{0,}b{1,}"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierNOrMoreModification)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "a{1,}b{1,}",
      "a{0,}b{0,}",
      "a{0,}b{2,}"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("QuantifierNOrMoreModification Does not mutate quantifier {n} and {n,m}") {
    val pattern = "a{3}b{4,9}"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierNOrMoreModification)) map (_.pattern)

    assertEquals(clue(mutants).length, 0)
  }

  test("Changes quantifier {n,}") {
    val pattern = "a{1,}"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierNOrMoreChange)) map (_.pattern)

    val expected: Seq[String] = Seq("a{1}")

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("QuantifierNOrMoreChange Does not mutate quantifier {n} and {n,m}") {
    val pattern = "a{3}b{4,9}"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierNOrMoreChange)) map (_.pattern)

    assertEquals(clue(mutants).length, 0)
  }

  test("Modifies quantifier {n,m}") {
    val pattern = "a{0,0}b{0,1}c{1,2}"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierNMModification)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "a{0,1}b{0,1}c{1,2}",
      "a{0,0}b{1,1}c{1,2}",
      "a{0,0}b{0,0}c{1,2}",
      "a{0,0}b{0,2}c{1,2}",
      "a{0,0}b{0,1}c{0,2}",
      "a{0,0}b{0,1}c{2,2}",
      "a{0,0}b{0,1}c{1,1}",
      "a{0,0}b{0,1}c{1,3}"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("QuantifierNMModification Does not mutate quantifier {n} and {n,}") {
    val pattern = "a{3}b{4,}"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierNMModification)) map (_.pattern)

    assertEquals(clue(mutants).length, 0)
  }

  test("Modifies short quantifier") {
    val pattern = "a?b*c+"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierShortModification)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "a{1,1}b*c+",
      "a{0,0}b*c+",
      "a{0,2}b*c+",
      "a?b{1,}c+",
      "a?b*c{0,}",
      "a?b*c{2,}"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Changes short quantifier") {
    val pattern = "a*b+"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierShortChange)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "a{0}b+",
      "a*b{1}"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Adds reluctant to greedy quantifier") {
    val pattern = "a?b*c+d{1}e{1,}f{1,2}"
    val parsedTree = Parser(pattern).fold(fail(_), identity)

    val mutants: Seq[String] = parsedTree.mutate(Seq(QuantifierReluctantAddition)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "a??b*c+d{1}e{1,}f{1,2}",
      "a?b*?c+d{1}e{1,}f{1,2}",
      "a?b*c+?d{1}e{1,}f{1,2}",
      "a?b*c+d{1}?e{1,}f{1,2}",
      "a?b*c+d{1}e{1,}?f{1,2}",
      "a?b*c+d{1}e{1,}f{1,2}?"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }
}
