package weaponregex.mutator

class MutatorTest extends munit.FunSuite {
  test("Mutator name is non-empty") {
    BuiltinMutators.all foreach (mutator => assert(clue(mutator).name.nonEmpty))
  }

  test("Mutator description is non-empty") {
    BuiltinMutators.all foreach (mutator => assert(clue(mutator).description.nonEmpty))
  }
}
