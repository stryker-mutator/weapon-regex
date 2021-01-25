package weaponregex.mutator

class BuiltinMutatorsTest extends munit.FunSuite {
  test("Convert to mutation levels map") {
    val levelsMap = BuiltinMutators.levels
    BuiltinMutators.all foreach (mutator =>
      mutator.levels foreach (level => assert(levelsMap(level) contains mutator, clue = (levelsMap(level), mutator)))
    )
  }

  test("Get mutators on level 1") {
    val level = 1
    BuiltinMutators.level(level) foreach (mutator => assert(clue(mutator).levels contains level))
  }

  test("Get mutators on level 2") {
    val level = 2
    BuiltinMutators.level(level) foreach (mutator => assert(clue(mutator).levels contains level))
  }

  test("Get mutators on level 3") {
    val level = 3
    BuiltinMutators.level(level) foreach (mutator => assert(clue(mutator).levels contains level))
  }

  test("Get mutators on an unsupported level") {
    val level = 100
    assertEquals(BuiltinMutators.level(level), Nil)
  }
}
