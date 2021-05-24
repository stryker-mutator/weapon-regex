package weaponregex.mutator

import weaponregex.extension.RegexTreeExtension.RegexTreeMutator
import weaponregex.model.regextree.RegexTree
import weaponregex.parser.Parser

class RegexTreeMutatorTest extends munit.FunSuite {

  val tree: RegexTree = Parser("""^(a*|b+(?=c)|[[c-z]XYZ]{3,}(ABC{4}DEF{5,9}\w)\p{Alpha})$""").get

  test("Filters mutators with level 1") {
    val levels = Seq(1)
    val mutants = tree.mutate(BuiltinMutators.all, levels)

    mutants foreach (mutant => assert(levels exists (clue(mutant).levels contains _)))
    assert(clue(mutants) exists (_.levels.length != 1))
  }

  test("Filters mutators with level 2") {
    val levels = Seq(2)
    val mutants = tree.mutate(BuiltinMutators.all, levels)

    mutants foreach (mutant => assert(levels exists (clue(mutant).levels contains _)))
    assert(clue(mutants) exists (_.levels.length != 1))
  }

  test("Filters mutators with level 3") {
    val levels = Seq(3)
    val mutants = tree.mutate(BuiltinMutators.all, levels)

    mutants foreach (mutant => assert(levels exists (clue(mutant).levels contains _)))
    assert(clue(mutants) exists (_.levels.length != 1))
  }

  test("Filters mutators with levels 1, 2") {
    val levels = Seq(1, 2)
    val mutants = tree.mutate(BuiltinMutators.all, levels)

    mutants foreach (mutant => assert(levels exists (clue(mutant).levels contains _)))
    assert(clue(mutants) exists (_.levels.length == 1))
  }

  test("Filters mutators with levels 2, 3") {
    val levels = Seq(2, 3)
    val mutants = tree.mutate(BuiltinMutators.all, levels)

    mutants foreach (mutant => assert(levels exists (clue(mutant).levels contains _)))
    assert(clue(mutants) exists (_.levels.length == 1))
  }

  test("Filters mutators with levels 1, 3") {
    val levels = Seq(1, 3)
    val mutants = tree.mutate(BuiltinMutators.all, levels)

    mutants foreach (mutant => assert(levels exists (clue(mutant).levels contains _)))
    assert(clue(mutants) exists (_.levels.length == 1))
  }

  test("Filters mutators with levels 1, 2, 3") {
    val levels = Seq(1, 2, 3)
    val mutants = tree.mutate(BuiltinMutators.all, levels)

    mutants foreach (mutant => assert(levels exists (clue(mutant).levels contains _)))
    assert(clue(mutants) exists (_.levels.length == 1))
  }

  test("Filters mutators with unsupported levels") {
    val levels = Seq(100, 1000)
    val mutants = tree.mutate(BuiltinMutators.all, levels)

    assert(clue(mutants).isEmpty)
  }

  test("Mutates with all mutators by default") {
    val mutants = tree.mutate()
    val mutantNames = mutants.map(_.name)
    BuiltinMutators.all foreach (mutator =>
      assert(mutantNames contains mutator.name, clue = (mutator.name, mutantNames))
    )
  }

  test("No filtering is done by default") {
    val mutants = tree.mutate(BuiltinMutators.all)
    val mutantNames = mutants.map(_.name)

    BuiltinMutators.all foreach (mutator =>
      assert(mutantNames contains mutator.name, clue = (mutator.name, mutantNames))
    )
  }

  test("Uses only the given mutators") {
    val usedMutators = BuiltinMutators.all.take(5)
    val excludedMutators = BuiltinMutators.all.drop(5)
    val mutants = tree.mutate(usedMutators)
    val mutantNames = mutants.map(_.name)

    usedMutators foreach (mutator => assert(mutantNames contains mutator.name, clue = (mutator.name, mutantNames)))

    excludedMutators foreach (mutator =>
      assert(!(mutantNames contains mutator.name), clue = (mutator.name, mutantNames))
    )
  }

  test("Uses only the given mutators with mutation levels filtering") {
    val usedMutators = BuiltinMutators.all.take(5)
    val excludedMutators = BuiltinMutators.all.drop(5)
    val levels = Seq(2, 3)
    val mutants = tree.mutate(usedMutators, levels)
    val mutantNames = mutants.map(_.name)

    usedMutators.filter(_.levels exists (levels contains _)) foreach (mutator =>
      assert(mutantNames contains mutator.name, clue = (mutator.name, mutantNames))
    )

    excludedMutators foreach (mutator =>
      assert(!(mutantNames contains mutator.name), clue = (mutator.name, mutantNames))
    )

    mutants foreach (mutant => assert(levels exists (clue(mutant).levels contains _)))
  }

  test("Used empty sequence of mutators") {
    val mutants = tree.mutate(Nil)
    assert(clue(mutants).isEmpty)
  }
}
