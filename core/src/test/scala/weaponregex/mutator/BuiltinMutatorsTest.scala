package weaponregex.mutator

import cats.data.NonEmptySet
import cats.syntax.all.*

class BuiltinMutatorsTest extends munit.FunSuite {
  test("Convert to a map by mutator class name") {
    val nameMap = BuiltinMutators.byName
    assertEquals(nameMap.keys.size, BuiltinMutators.all.length.toLong)
    BuiltinMutators.all.toList.foreach(mutator =>
      assertEquals(nameMap(mutator.getClass.getSimpleName.stripSuffix("$")).get, mutator)
    )
  }

  test("Convert to a map by mutation levels") {
    val levelMap = BuiltinMutators.byLevel
    BuiltinMutators.all.toList.foreach(mutator =>
      mutator.levels.toList.foreach(level =>
        assert(levelMap(level).get.toList.contains(mutator), clue = (levelMap(level), mutator))
      )
    )
  }

  test("Get mutator in a single level") {
    val level = 1
    val mutators = BuiltinMutators.atLevel(level).get
    mutators.toList.foreach(mutator => assert(clue(mutator).levels.contains(level)))
  }

  test("Get mutator in a single non-existed level") {
    val level = -1
    val mutators = BuiltinMutators.atLevel(level)
    assertEquals(clue(mutators), None)
  }

  test("Get mutator in multiple levels") {
    val levels = NonEmptySet.of(1, 2, 3)
    val mutators = BuiltinMutators.atLevels(levels).get.toList
    mutators.foreach(mutator => assert(levels.exists(clue(mutator).levels.contains(_))))
  }

  test("Get mutator in multiple levels with some non-existed levels") {
    val levels = NonEmptySet.of(1, 2, 3)
    val badLevels = NonEmptySet.of(-1, -2, -3)
    val mutators = BuiltinMutators.atLevels(levels ++ badLevels).get
    mutators.toList.foreach(mutator => {
      assert(levels.exists(clue(mutator).levels.contains(_)))
      assert(!badLevels.exists(clue(mutator).levels.contains(_)))
    })
  }

  test("Get mutator in non-existed multiple levels") {
    val levels = NonEmptySet.of(-1, -2, -3)
    val mutators = BuiltinMutators.atLevels(levels)
    assertEquals(clue(mutators), None)
  }
}
