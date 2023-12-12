package weaponregex.internal.model.regextree

import weaponregex.internal.constant.RegexTreeStubs.LOCATION
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeStringBuilder

class ReferenceNodeTest extends munit.FunSuite {
  test("NameReference build") {
    val node1 = NameReference("name", LOCATION)
    assertEquals(node1.build, """\k<name>""")
  }

  test("NumberReference build") {
    val node1 = NumberReference(4, LOCATION)
    assertEquals(node1.build, """\4""")
  }
}
