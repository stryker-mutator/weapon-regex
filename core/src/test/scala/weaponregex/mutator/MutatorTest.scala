package weaponregex.mutator

import weaponregex.internal.constant.RegexTreeStubs.LOCATION

class MutatorTest extends munit.FunSuite {
  test("Mutator name is non-empty") {
    BuiltinMutators.all foreach (mutator => assert(clue(mutator).name.nonEmpty))
  }

  test("Mutator description starts with a location") {
    BuiltinMutators.all foreach (mutator =>
      assert(clue(mutator.description("original", "mutated", LOCATION)).startsWith(LOCATION.show))
    )
  }
}
