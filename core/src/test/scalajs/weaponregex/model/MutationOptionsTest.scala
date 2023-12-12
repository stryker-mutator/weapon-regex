package weaponregex.model

import weaponregex.parser.ParserFlavorJS

class MutationOptionsTest extends munit.FunSuite {
  test("Default mutation options") {
    val options = new MutationOptions
    assertEquals(options.mutators, null)
    assertEquals(options.mutationLevels, null)
    assertEquals(options.flavor, ParserFlavorJS)
  }
}
