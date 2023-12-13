package weaponregex.internal.extension

import weaponregex.internal.extension.TokenMutatorExtension.TokenMutatorsFiltering
import weaponregex.mutator.BuiltinMutators

class TokenMutatorExtensionTest extends munit.FunSuite {
  test("Filter a single level") {
    val level = 1
    val mutators = BuiltinMutators.all.atLevel(level)
    mutators foreach (mutator => assert(clue(mutator).levels contains level))
  }

  test("Filter a single non-existed level") {
    val level = -1
    val mutators = BuiltinMutators.all.atLevel(level)
    assert(clue(mutators) == Nil)
  }

  test("Filter multiple levels") {
    val levels = Seq(1, 2, 3)
    val mutators = BuiltinMutators.all.atLevels(levels)
    mutators foreach (mutator => assert(levels exists (clue(mutator).levels contains _)))
  }

  test("Filter multiple levels with some non-existed levels") {
    val levels = Seq(1, 2, 3)
    val badLevels = Seq(-1, -2, -3)
    val mutators = BuiltinMutators.all.atLevels(levels ++ badLevels)
    mutators foreach (mutator => {
      assert(levels exists (clue(mutator).levels contains _))
      assert(!(badLevels exists (clue(mutator).levels contains _)))
    })
  }

  test("Filter non-existed multiple levels") {
    val levels = Seq(-1, -2, -3)
    val mutators = BuiltinMutators.all.atLevels(levels)
    assert(clue(mutators) == Nil)
  }
}
