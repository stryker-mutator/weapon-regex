package weaponregex.internal.extension

import cats.data.NonEmptySet
import cats.syntax.all.*
import weaponregex.internal.extension.TokenMutatorExtension.TokenMutatorsFiltering
import weaponregex.mutator.BuiltinMutators

class TokenMutatorExtensionTest extends munit.FunSuite {
  test("Filter a single level") {
    val level = 1
    val mutators = BuiltinMutators.all.atLevel(level).get
    mutators.toList.foreach(mutator => assert(clue(mutator).levels.contains(level)))
  }

  test("Filter a single non-existed level") {
    val level = -1
    val mutators = BuiltinMutators.all.atLevel(level)
    assertEquals(mutators, None)
  }

  test("Filter multiple levels") {
    val levels = NonEmptySet.of(1, 2, 3)
    val mutators = BuiltinMutators.all.atLevels(levels).get
    mutators.toList.foreach(mutator => assert(levels.exists(clue(mutator).levels.contains(_))))
  }

  test("Filter multiple levels with some non-existed levels") {
    val levels = NonEmptySet.of(1, 2, 3)
    val badLevels = NonEmptySet.of(-1, -2, -3)
    val mutators = BuiltinMutators.all.atLevels(levels ++ badLevels).get
    mutators.toList.foreach(mutator => {
      assert(levels.exists(clue(mutator).levels.contains(_)))
      assert(!badLevels.exists(clue(mutator).levels.contains(_)))
    })
  }

  test("Filter non-existed multiple levels") {
    val levels = NonEmptySet.of(-1, -2, -3)
    val mutators = BuiltinMutators.all.atLevels(levels)
    assertEquals(mutators, None)
  }
}
