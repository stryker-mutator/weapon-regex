package weaponregex.internal.model.regextree

import weaponregex.internal.constant.RegexTreeStubs.LOCATION
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeStringBuilder

class CharacterNodeTest extends munit.FunSuite {
  test("Character build a") {
    val node1 = Character('a', LOCATION)
    assertEquals(node1.build, "a")
  }

  test("Character build b") {
    val node2 = Character('b', LOCATION)
    assertEquals(node2.build, "b")
  }

  test("Any dot build") {
    val node1 = AnyDot(LOCATION)
    assertEquals(node1.build, ".")
  }

  test("MetaChar build a") {
    val node1 = MetaChar("a", LOCATION)
    assertEquals(node1.build, """\a""")
  }

  test("MetaChar build 0123") {
    val node2 = MetaChar("0123", LOCATION)
    assertEquals(node2.build, """\0123""")
  }
}
