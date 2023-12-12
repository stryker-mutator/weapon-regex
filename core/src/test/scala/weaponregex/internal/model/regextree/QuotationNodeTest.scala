package weaponregex.internal.model.regextree

import weaponregex.internal.constant.RegexTreeStubs.LOCATION
import weaponregex.internal.extension.RegexTreeExtension.RegexTreeStringBuilder

class QuotationNodeTest extends munit.FunSuite {
  test("QuoteChar build") {
    val node1 = QuoteChar('y', LOCATION)
    assertEquals(node1.build, """\y""")
  }

  test("QuoteChar build") {
    val node1 = QuoteChar('y', LOCATION)
    assertEquals(node1.build, """\y""")
  }

  test("Quote build with end") {
    val node1 = Quote("some quote", hasEnd = false, LOCATION)
    assertEquals(node1.build, """\Qsome quote""")
  }

  test("Quote build without end") {
    val node2 = Quote("some quote", hasEnd = true, LOCATION)
    assertEquals(node2.build, """\Qsome quote\E""")
  }
}
