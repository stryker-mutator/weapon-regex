package weaponregex.internal.extension

import weaponregex.internal.extension.MutationOptionsExtension.MutationOptionsConverter
import weaponregex.model.MutationOptions
import weaponregex.mutator.{BuiltinMutators, BuiltinMutatorsJS}
import weaponregex.parser.{ParserFlavorJS, ParserFlavorJVM}

import scala.scalajs.js

class MutationOptionsExtensionTest extends munit.FunSuite {
  test("Convert default mutation options to Scala") {
    val options = new MutationOptions
    val (mutators, mutationLevels, flavor) = options.toScala

    assertEquals(mutators, BuiltinMutators.all)
    assertEquals(mutationLevels, null)
    assertEquals(flavor, ParserFlavorJS)
  }

  test("Convert mutation options with custom mutators to Scala") {
    val numMutators = 3
    val mutatorsIn = BuiltinMutatorsJS.all.take(numMutators)

    val options = new MutationOptions(mutators = mutatorsIn)
    val (mutators, mutationLevels, flavor) = options.toScala

    assertEquals(mutators, BuiltinMutators.all.take(numMutators))
    assertEquals(mutationLevels, null)
    assertEquals(flavor, ParserFlavorJS)
  }

  test("Convert mutation options with custom mutation levels to Scala") {
    val levelsIn = js.Array(2, 3)

    val options = new MutationOptions(mutationLevels = levelsIn)
    val (mutators, mutationLevels, flavor) = options.toScala

    assertEquals(mutators, BuiltinMutators.all)
    assertEquals(mutationLevels, levelsIn.toSeq)
    assertEquals(flavor, ParserFlavorJS)
  }

  test("Convert mutation options with custom mutation levels to Scala") {
    val flavorIn = ParserFlavorJVM

    val options = new MutationOptions(flavor = flavorIn)
    val (mutators, mutationLevels, flavor) = options.toScala

    assertEquals(mutators, BuiltinMutators.all)
    assertEquals(mutationLevels, null)
    assertEquals(flavor, flavorIn)
  }

  test("Convert fully custom mutation options to Scala") {
    val numMutators = 3
    val mutatorsIn = BuiltinMutatorsJS.all.take(numMutators)
    val levelsIn = js.Array(2, 3)
    val flavorIn = ParserFlavorJVM

    val options = new MutationOptions(mutatorsIn, levelsIn, flavorIn)
    val (mutators, mutationLevels, flavor) = options.toScala

    assertEquals(mutators, BuiltinMutators.all.take(numMutators))
    assertEquals(mutationLevels, levelsIn.toSeq)
    assertEquals(flavor, flavorIn)
  }
}
