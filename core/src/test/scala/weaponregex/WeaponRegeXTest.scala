package weaponregex

import weaponregex.constant.ErrorMessage
import weaponregex.model.mutation.Mutant
import weaponregex.mutator.BuiltinMutators

class WeaponRegeXTest extends munit.FunSuite {
  test("Can mutate without options") {
    val mutations = WeaponRegeX.mutate("^a").fold(fail(_), identity)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations.length, 2)
  }

  test("Can mutate with only mutators as option") {
    val mutations = WeaponRegeX.mutate("^a", BuiltinMutators.all).fold(fail(_), identity)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations.length, 2)
  }

  test("Can mutate with empty sequence of mutators as option") {
    val mutations = WeaponRegeX.mutate("^a", Nil).fold(fail(_), identity)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations, Nil)
  }

  test("Can mutate with only levels as option") {
    val mutations = WeaponRegeX.mutate("^a", mutationLevels = Seq(1)).fold(fail(_), identity)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations.length, 1)
  }

  test("Can mutate with unsupported levels as option") {
    val mutations = WeaponRegeX.mutate("^a", mutationLevels = Seq(100, 1000)).fold(fail(_), identity)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations, Nil)
  }

  test("Can mutate with both mutators and levels as option") {
    val mutations = WeaponRegeX.mutate("^a", BuiltinMutators.all, Seq(1)).fold(fail(_), identity)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations.length, 1)
  }

  test("Returns an empty sequence if there are no mutants") {
    val mutations = WeaponRegeX.mutate("a").fold(fail(_), identity)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations, Nil)
  }

  test("Returns a Left with error message if the regex is invalid") {
    val mutations = WeaponRegeX.mutate("*(a|$]")

    assertEquals(mutations, Left("[Error] Parser: Position 1:1, found \"*(a|$]\""))
  }

  test("Returns when Parser failed") {
    val mutations = WeaponRegeX.mutate("(")

    assert(clue(mutations) match {
      case Left(msg) => msg.startsWith(ErrorMessage.parserErrorHeader)
      case _         => false
    })
  }
}
