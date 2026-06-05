package weaponregex.internal.mutator

import cats.data.NonEmptySet
import cats.syntax.all.*
import munit.Location
import weaponregex.internal.extension.EitherExtension.LeftStringEitherTest
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeMutator
import weaponregex.internal.model.regextree.RegexTree
import weaponregex.internal.parser.Parser
import weaponregex.model.mutation.Mutant
import weaponregex.mutator.BuiltinMutators

class RegexTreeMutatorTest extends munit.FunSuite {

  val regex = """^(a*|b+(?=c)|[[c-z]XYZ]{3,}(ABC{4}DEF{5,9}\w)\p{Alpha})$"""
  val tree: RegexTree =
    Parser(regex).getOrFail

  test("Filters mutators with level 1") {
    val levels = NonEmptySet.of(1)
    val mutants = tree.mutate(BuiltinMutators.all, levels.some)

    mutants.foreach(mutant => assert(levels.exists(clue(mutant).levels.contains(_))))
    assert(clue(mutants) exists (_.levels.length != 1))
  }

  test("Filters mutators with level 2") {
    val levels = NonEmptySet.of(2)
    val mutants = tree.mutate(BuiltinMutators.all, levels.some)

    mutants.foreach(mutant => assert(levels.exists(clue(mutant).levels.contains(_))))
    assert(clue(mutants) exists (_.levels.length != 1))
  }

  test("Filters mutators with level 3") {
    val levels = NonEmptySet.of(3)
    val mutants = tree.mutate(BuiltinMutators.all, levels.some)

    mutants.foreach(mutant => assert(levels.exists(clue(mutant).levels.contains(_))))
    assert(clue(mutants) exists (_.levels.length != 1))
  }

  test("Filters mutators with levels 1, 2") {
    val levels = NonEmptySet.of(1, 2)
    val mutants = tree.mutate(BuiltinMutators.all, levels.some)

    mutants.foreach(mutant => assert(levels.exists(clue(mutant).levels.contains(_))))
    assert(clue(mutants) exists (_.levels.length == 1))
  }

  test("Filters mutators with levels 2, 3") {
    val levels = NonEmptySet.of(2, 3)
    val mutants = tree.mutate(BuiltinMutators.all, levels.some)

    mutants.foreach(mutant => assert(levels.exists(clue(mutant).levels.contains(_))))
    assert(clue(mutants) exists (_.levels.length == 1))
  }

  test("Filters mutators with levels 1, 3") {
    val levels = NonEmptySet.of(1, 3)
    val mutants = tree.mutate(BuiltinMutators.all, levels.some)

    mutants.foreach(mutant => assert(levels.exists(clue(mutant).levels.contains(_))))
    assert(clue(mutants) exists (_.levels.length == 1))
  }

  test("Filters mutators with levels 1, 2, 3") {
    val levels = NonEmptySet.of(1, 2, 3)
    val mutants = tree.mutate(BuiltinMutators.all, levels.some)

    mutants.foreach(mutant => assert(levels.exists(clue(mutant).levels.contains(_))))
    assert(clue(mutants) exists (_.levels.length == 1))
  }

  test("Filters mutators with unsupported levels") {
    val levels = NonEmptySet.of(100, 1000)
    val mutants = tree.mutate(BuiltinMutators.all, levels.some)

    assert(clue(mutants).isEmpty)
  }

  test("Mutates with all mutators by default") {
    val mutants = tree.mutate()
    val mutantNames = mutants.map(_.name)
    BuiltinMutators.all.toList.foreach(mutator =>
      assert(mutantNames contains mutator.name, clue = (mutator.name, mutantNames))
    )
  }

  test("No filtering is done by default") {
    val mutants = tree.mutate(BuiltinMutators.all)
    val mutantNames = mutants.map(_.name)

    BuiltinMutators.all.toList.foreach(mutator =>
      assert(mutantNames contains mutator.name, clue = (mutator.name, mutantNames))
    )
  }

  test("Uses only the given mutators") {
    val usedMutators = BuiltinMutators.all.take(5)
    val excludedMutators = BuiltinMutators.all.toList.drop(5)
    val mutants = tree.mutate(usedMutators.toNel.get)
    val mutantNames = mutants.map(_.name)

    usedMutators foreach (mutator => assert(mutantNames contains mutator.name, clue = (mutator.name, mutantNames)))

    excludedMutators foreach (mutator =>
      assert(!(mutantNames contains mutator.name), clue = (mutator.name, mutantNames))
    )
  }

  test("Uses only the given mutators with mutation levels filtering") {
    val usedMutators = BuiltinMutators.all.take(5)
    val excludedMutators = BuiltinMutators.all.toList.drop(5)
    val levels = NonEmptySet.of(2, 3)
    val mutants = tree.mutate(usedMutators.toNel.get, levels.some)
    val mutantNames = mutants.map(_.name)

    usedMutators.filter(_.levels.exists(levels.contains(_))) foreach (mutator =>
      assert(mutantNames contains mutator.name, clue = (mutator.name, mutantNames))
    )

    excludedMutators foreach (mutator =>
      assert(!(mutantNames contains mutator.name), clue = (mutator.name, mutantNames))
    )

    mutants.foreach(mutant => assert(levels.exists(clue(mutant).levels.contains(_))))
  }

  test("Adds correct replacement") {
    val regex = """^a{4,}[ab]$"""
    val tree = Parser(regex).getOrFail
    val mutants = tree.mutate(BuiltinMutators.atLevels(NonEmptySet.one(2)).get)

    val expected = Seq[(String, String, Location)](
      ("a{4,}[ab]$", "", implicitly),
      ("^a{4,}[ab]", "", implicitly),
      ("\\Aa{4,}[ab]$", "\\A", implicitly),
      ("^a{3,}[ab]$", "{3,}", implicitly),
      ("^a{5,}[ab]$", "{5,}", implicitly),
      ("^a{4}[ab]$", "{4}", implicitly),
      ("^a{4,}[b]$", "", implicitly),
      ("^a{4,}[a]$", "", implicitly),
      ("^a{4,}[\\w\\W]$", "[\\w\\W]", implicitly),
      ("^a{4,}[ab]\\z", "\\z", implicitly)
    )

    assertEquals(mutants.length, expected.length, mutants.map(_.replacement))
    expected.zipWithIndex.foreach { case ((pattern, replacement, loc), i) =>
      implicit val location: Location = loc
      val m = mutants(i)

      assertEquals(m.pattern, pattern, clue = s"${m.name} ${m.description}")
      assertEquals(m.replacement, replacement, clue = s"${m.name} ${m.description}")
      assertReplacementPosition(m, regex, i)
    }
  }

  test("replacement position is accurate") {
    tree.mutate(BuiltinMutators.all).zipWithIndex.foreach { case (mutant, i) =>
      assertReplacementPosition(mutant, regex, i)
    }
  }

  def assertReplacementPosition(mutant: Mutant, regex: String, i: Int)(implicit loc: Location): Unit = {
    val start = regex.substring(0, mutant.location.start.column)
    val end = regex.substring(mutant.location.end.column)

    assertNoDiff(
      start + mutant.replacement + end,
      mutant.pattern,
      clue = s"$i: ${mutant.name} ${mutant.description}"
    )
  }

}
