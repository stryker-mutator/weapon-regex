package weaponregex.internal.mutator

import weaponregex.internal.extension.EitherExtension.LeftStringEitherTest
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeMutator
import weaponregex.internal.parser.Parser

class CharClassMutatorTest extends munit.FunSuite {
  test("Negates Character Classes") {
    val pattern = "[[abc][^abc]]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassNegation)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "[^[abc][^abc]]",
      "[[^abc][^abc]]",
      "[[abc][abc]]"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Does not mutate escaped Character Classes") {
    val pattern = "\\[abc\\]abc"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassNegation)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }

  test("Removes children of Character Classes") {
    val pattern = "[ab0-9[A-Z][cd]]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassChildRemoval)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "[b0-9[A-Z][cd]]",
      "[a0-9[A-Z][cd]]",
      "[ab[A-Z][cd]]",
      "[ab0-9[cd]]",
      "[ab0-9[A-Z]]",
      "[ab0-9[A-Z][d]]",
      "[ab0-9[A-Z][c]]"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Removes children of Naked Character Classes") {
    val pattern = "[abc&&def&&[gh]]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassChildRemoval)) map (_.pattern)

    val expected: Seq[String] = Seq(
      "[bc&&def&&[gh]]",
      "[ac&&def&&[gh]]",
      "[ab&&def&&[gh]]",
      "[abc&&ef&&[gh]]",
      "[abc&&df&&[gh]]",
      "[abc&&de&&[gh]]",
      "[abc&&def&&[h]]",
      "[abc&&def&&[g]]"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Does not mutate (remove children) escaped Character Classes") {
    val pattern = "\\[abc\\]abc"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassChildRemoval)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }

  test("Character Class to any char") {
    val pattern = "[abc[0-9]]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassAnyChar)) map (_.pattern)

    val expected: Seq[String] = Seq(
      """[\w\W]""",
      """[abc[\w\W]]"""
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Does not mutate (change to any char) escaped Character Classes") {
    val pattern = "\\[abc\\]abc"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassAnyChar)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }

  test("Character Class Modify Range [b-y][B-Y][1-8]") {
    val pattern = "[b-y][B-Y][1-8]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassRangeModification)) map (_.pattern)

    val expected: Seq[String] = Seq(
      // [b-y] -> [a-y] or [c-y] or [b-z] or [b-x]
      "[a-y][B-Y][1-8]",
      "[c-y][B-Y][1-8]",
      "[b-z][B-Y][1-8]",
      "[b-x][B-Y][1-8]",
      // [B-Y] -> [A-Y] OR [C-Y] OR [B-Z] OR [B-X]
      "[b-y][A-Y][1-8]",
      "[b-y][C-Y][1-8]",
      "[b-y][B-Z][1-8]",
      "[b-y][B-X][1-8]",
      // [1-8] -> [0-8] OR [2-8] OR [1-9] OR [1-7]
      "[b-y][B-Y][0-8]",
      "[b-y][B-Y][2-8]",
      "[b-y][B-Y][1-9]",
      "[b-y][B-Y][1-7]"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Character Class Modify Range [a-y][A-Y][0-8]") {
    val pattern = "[a-y][A-Y][0-8]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassRangeModification)) map (_.pattern)

    val expected: Seq[String] = Seq(
      // [a-y] -> [b-y] or [a-z] or [a-x]
      "[b-y][A-Y][0-8]",
      "[a-z][A-Y][0-8]",
      "[a-x][A-Y][0-8]",
      // [A-Y] -> [B-Y] OR [A-Z] OR [A-X]
      "[a-y][B-Y][0-8]",
      "[a-y][A-Z][0-8]",
      "[a-y][A-X][0-8]",
      // [0-8] -> [1-8] OR [0-9] OR [0-7]
      "[a-y][A-Y][1-8]",
      "[a-y][A-Y][0-9]",
      "[a-y][A-Y][0-7]"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Character Class Modify Range [b-z][B-Z][1-9]") {
    val pattern = "[b-z][B-Z][1-9]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassRangeModification)) map (_.pattern)

    val expected: Seq[String] = Seq(
      // [b-z] -> [a-z] or [c-z] or [b-y]
      "[a-z][B-Z][1-9]",
      "[c-z][B-Z][1-9]",
      "[b-y][B-Z][1-9]",
      // [B-Z] -> [A-Z] OR [C-Z] OR [B-Y]
      "[b-z][A-Z][1-9]",
      "[b-z][C-Z][1-9]",
      "[b-z][B-Y][1-9]",
      // [1-9] -> [0-9] OR [2-9] OR [1-8]
      "[b-z][B-Z][0-9]",
      "[b-z][B-Z][2-9]",
      "[b-z][B-Z][1-8]"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Character Class Modify Range [a-z][A-Z][0-9]") {
    val pattern = "[a-z][A-Z][0-9]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassRangeModification)) map (_.pattern)

    val expected: Seq[String] = Seq(
      // [a-z] -> [b-z] or [a-y]
      "[b-z][A-Z][0-9]",
      "[a-y][A-Z][0-9]",
      // [A-Z] -> [B-Z] OR [A-Y]
      "[a-z][B-Z][0-9]",
      "[a-z][A-Y][0-9]",
      // [0-9] -> [1-9] OR [0-8]
      "[a-z][A-Z][1-9]",
      "[a-z][A-Z][0-8]"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Character Class Modify Range [b-b][B-B][1-1]") {
    val pattern = "[b-b][B-B][1-1]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassRangeModification)) map (_.pattern)

    val expected: Seq[String] = Seq(
      // [b-b] -> [a-b] or [b-c]
      "[a-b][B-B][1-1]",
      "[b-c][B-B][1-1]",
      // [B-B] -> [A-B] OR [B-C]
      "[b-b][A-B][1-1]",
      "[b-b][B-C][1-1]",
      // [1-1] -> [0-1] OR [1-2]
      "[b-b][B-B][0-1]",
      "[b-b][B-B][1-2]"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Character Class Modify Range [a-a][A-A][0-0]") {
    val pattern = "[a-a][A-A][0-0]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassRangeModification)) map (_.pattern)

    val expected: Seq[String] = Seq(
      // [a-a] -> [a-b]
      "[a-b][A-A][0-0]",
      // [A-A] -> [A-B]
      "[a-a][A-B][0-0]",
      // [0-0] -> [0-1]
      "[a-a][A-A][0-1]"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Character Class Modify Range [z-z][Z-Z][9-9]") {
    val pattern = "[z-z][Z-Z][9-9]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassRangeModification)) map (_.pattern)

    val expected: Seq[String] = Seq(
      // [z-z] -> [y-z]
      "[y-z][Z-Z][9-9]",
      // [Z-Z] -> [Y-Z]
      "[z-z][Y-Z][9-9]",
      // [9-9] -> [8-9]
      "[z-z][Z-Z][8-9]"
    )

    assertEquals(clue(mutants).length, expected.length)
    expected foreach (m => assert(clue(mutants) contains clue(m)))
  }

  test("Does not modify non alpha numeric ranges") {
    val pattern = "[!-#][a-#][!-z][A-#][!-Z][1-#][!-8]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassRangeModification)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }

  test("Does not modify ranges with letters and digits mixed") {
    val pattern = "[a-8][1-z][A-8][1-Z]"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassRangeModification)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }

  test("Does not mutate (modify range) escaped Character Classes") {
    val pattern = "\\[a-z\\]a-z"
    val parsedTree = Parser(pattern).getOrFail

    val mutants: Seq[String] = parsedTree.mutate(Seq(CharClassRangeModification)) map (_.pattern)
    assertEquals(clue(mutants), Nil)
  }
}
