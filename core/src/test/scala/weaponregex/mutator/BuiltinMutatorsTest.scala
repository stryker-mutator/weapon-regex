package weaponregex.mutator

class BuiltinMutatorsTest extends munit.FunSuite {
  test("Convert to a map by mutator class name") {
    val nameMap = BuiltinMutators.byName
    assertEquals(nameMap.keys.size, BuiltinMutators.all.length)
    BuiltinMutators.all foreach (mutator =>
      assertEquals(nameMap(mutator.getClass.getSimpleName.stripSuffix("$")), mutator)
    )
  }

  test("Convert to a map by mutation levels") {
    val levelMap = BuiltinMutators.byLevel
    BuiltinMutators.all foreach (mutator =>
      mutator.levels foreach (level => assert(levelMap(level) contains mutator, clue = (levelMap(level), mutator)))
    )
  }

  test("Get mutator in a single level") {
    val level = 1
    val mutators = BuiltinMutators.atLevel(level)
    mutators foreach (mutator => assert(clue(mutator).levels contains level))
  }

  test("Get mutator in a single non-existed level") {
    val level = -1
    val mutators = BuiltinMutators.atLevel(level)
    assert(clue(mutators) == Nil)
  }

  test("Get mutator in multiple levels") {
    val levels = Seq(1, 2, 3)
    val mutators = BuiltinMutators.atLevels(levels)
    mutators foreach (mutator => assert(levels exists (clue(mutator).levels contains _)))
  }

  test("Get mutator in multiple levels with some non-existed levels") {
    val levels = Seq(1, 2, 3)
    val badLevels = Seq(-1, -2, -3)
    val mutators = BuiltinMutators.atLevels(levels ++ badLevels)
    mutators foreach (mutator => {
      assert(levels exists (clue(mutator).levels contains _))
      assert(!(badLevels exists (clue(mutator).levels contains _)))
    })
  }

  test("Get mutator in non-existed multiple levels") {
    val levels = Seq(-1, -2, -3)
    val mutators = BuiltinMutators.atLevels(levels)
    assert(clue(mutators) == Nil)
  }
}
