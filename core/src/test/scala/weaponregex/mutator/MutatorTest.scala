package weaponregex.mutator

import weaponregex.model.Location
import weaponregex.model.regextree.AnyDot

class MutatorTest extends munit.FunSuite {
  test("Mutator name is non-empty") {
    BuiltinMutators.all foreach (mutator => assert(clue(mutator).name.nonEmpty))
  }

  test("Mutator description is non-empty") {
    BuiltinMutators.all foreach (mutator => assert(clue(mutator).description.nonEmpty))
  }

  test("Mutation description has at least correct start position") {
    val location = Location(1, 2)(3, 4)
    val dummyToken = AnyDot(location)

    BuiltinMutators.all foreach (mutator =>
      assert(clue(mutator.describeMutation(dummyToken, location)).contains(clue(location.start.toString)))
    )
  }
}
