package weaponregex

import weaponregex.mutator.BuiltinMutators
import weaponregex.model.mutation.Mutant

class WeaponRegeXTest extends munit.FunSuite {
  test("Can mutate without options") {
    val mutations = WeaponRegeX.mutate("^a").getOrElse(Nil)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations.length, 2)
  }

  test("Can mutate with only mutators as option") {
    val mutations = WeaponRegeX.mutate("^a", BuiltinMutators.all).getOrElse(Nil)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations.length, 2)
  }

  test("Can mutate with empty sequence of mutators as option") {
    val mutations = WeaponRegeX.mutate("^a", Nil).getOrElse(Nil)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations, Nil)
  }

  test("Can mutate with only levels as option") {
    val mutations = WeaponRegeX.mutate("^a", mutationLevels = Seq(1)).getOrElse(Nil)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations.length, 1)
  }

  test("Can mutate with unsupported levels as option") {
    val mutations = WeaponRegeX.mutate("^a", mutationLevels = Seq(100, 1000)).getOrElse(Nil)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations, Nil)
  }

  test("Can mutate with both mutators and levels as option") {
    val mutations = WeaponRegeX.mutate("^a", BuiltinMutators.all, Seq(1)).getOrElse(Nil)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations.length, 1)
  }

  test("Returns an empty sequence if there are no mutants") {
    val mutations = WeaponRegeX.mutate("a").getOrElse(Nil)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations, Nil)
  }

  test("Returns an empty sequence if the regex is invalid") {
    val mutations = WeaponRegeX.mutate("*(a|$]").getOrElse(Nil)

    assert(mutations.isInstanceOf[Seq[Mutant]])
    assertEquals(mutations, Nil)
  }

  test("Returns when Parser failed") {
    val mutations = WeaponRegeX.mutate("(")

    import scala.util.Failure
    assert(clue(mutations) match {
      case Failure(exception: RuntimeException) => exception.getMessage.startsWith("[Error] Parser:")
      case _                                    => false
    })
  }
}
