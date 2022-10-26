package weaponregex.model.regextree

import weaponregex.constant.RegexTreeStubs.LOCATION
import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder

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
