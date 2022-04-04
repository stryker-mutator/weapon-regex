package weaponregex.parser

import weaponregex.model.regextree.*

class ParserJSTest extends munit.FunSuite with ParserTest {
  final val parserFlavor: ParserFlavor = ParserFlavorJS

  val boundaryMetacharacters: String = """\b\B"""
  val charClassPredefCharClasses: String = """\d\D\s\S\v\w\W"""
  val charClassSpecialChars: String = "(){}.^$|?*+"
  val escapeCharacters: String = """\\\t\n\r\f"""
  val hexCharacters: String = "\\x20\\u0020"
  val octCharacters: String = """\1\12\123"""
  val predefCharClasses: String = "." + charClassPredefCharClasses

  test("""Not parse `\A\G\z\Z` as boundary metacharacters""") {
    val pattern = """\A\G\z\Z"""
    val parsedTree = Parser(pattern, parserFlavor).get.to[Node]

    parsedTree.children foreach (child => assert(!clue(child).isInstanceOf[Boundary]))

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse empty positive character class with characters") {
    val pattern = "[]"
    val parsedTree = Parser(pattern, parserFlavor).get.to[CharacterClass]

    assert(clue(parsedTree.children).isEmpty)

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse negative character class with characters") {
    val pattern = "[^]"
    val parsedTree = Parser(pattern, parserFlavor).get.to[CharacterClass]

    assert(!parsedTree.isPositive)
    assert(clue(parsedTree.children).isEmpty)

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `[` as character inside a character class") {
    val pattern = "[[]"
    val parsedTree = Parser(pattern, parserFlavor).get.to[CharacterClass]

    assert(clue(parsedTree.children.head) match {
      case Character('[', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("""Not parse `\a\e` as escape characters""") {
    val pattern = """\a\e"""
    val parsedTree = Parser(pattern, parserFlavor).get.to[Concat]

    parsedTree.children foreach (child => assert(!clue(child).isInstanceOf[MetaChar]))

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse \\x{20} as quantifier") {
    val pattern = "\\x{20}"
    val parsedTree = Parser(pattern, parserFlavor).get.to[Quantifier]

    assert(clue(parsedTree) match {
      case Quantifier(QuoteChar('x', _), 20, 20, _, GreedyQuantifier, true) => true
      case _                                                                => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("""Not parse `\h\H\V` as predefined character class""") {
    val pattern = """\h\H\V"""
    val parsedTree = Parser(pattern, parserFlavor).get.to[Concat]

    parsedTree.children foreach (child => assert(!clue(child).isInstanceOf[PredefinedCharClass]))

    treeBuildTest(parsedTree, pattern)
  }

  test("Unparsable: flag toggle group i-i") {
    val pattern = "(?idmsuxU-idmsuxU)"
    parseErrorTest(pattern)
  }

  test("Unparsable: flag toggle group i-") {
    val pattern = "(?idmsuxU-)"
    parseErrorTest(pattern)
  }

  test("Unparsable: flag toggle group -i") {
    val pattern = "(?-idmsuxU)"
    parseErrorTest(pattern)
  }

  test("Unparsable: flag toggle group -") {
    val pattern = "(?-)"
    parseErrorTest(pattern)
  }

  test("Unparsable: flag toggle group i") {
    val pattern = "(?idmsuxU)"
    parseErrorTest(pattern)
  }

  test("Unparsable: non-capturing group with flags i-i") {
    val pattern = "(?idmsux-idmsux:hello)"
    parseErrorTest(pattern)
  }

  test("Unparsable: non-capturing group with flags i-") {
    val pattern = "(?idmsux-:hello)"
    parseErrorTest(pattern)
  }

  test("Unparsable: non-capturing group with flags -i") {
    val pattern = "(?-idmsux:hello)"
    parseErrorTest(pattern)
  }

  test("Unparsable: non-capturing group with flags -") {
    val pattern = "(?-:hello)"
    parseErrorTest(pattern)
  }

  test("Unparsable: non-capturing group with flags i") {
    val pattern = "(?idmsux:hello)"
    parseErrorTest(pattern)
  }

  test("Unparsable: independent non-capturing group") {
    val pattern = "(?>hello)"
    parseErrorTest(pattern)
  }

  test("""Parse `\Q\E` as character quotes""") {
    val pattern = """stuff\Q$hit\Emorestuff"""
    val parsedTree = Parser(pattern, parserFlavor).get.to[Concat]

    assert(clue(parsedTree.children(5)) match {
      case QuoteChar('Q', _) => true
      case _                 => false
    })
    assert(clue(parsedTree.children(10)) match {
      case QuoteChar('E', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("""Parse `\Q` as a character quote""") {
    val pattern = """stuff\Q$hit"""
    val parsedTree = Parser(pattern, parserFlavor).get.to[Concat]

    assert(clue(parsedTree.children(5)) match {
      case QuoteChar('Q', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Unparsable: long-quantifier-like with nothing preceding") {
    val patterns = Seq(
      "{1}",
      "{1,}",
      "{1,2}"
    )
    patterns map parseErrorTest
  }

  test("Parse `{`") {
    val pattern = "{"
    val parsedTree = Parser(pattern, parserFlavor).get.to[Character]

    assertEquals(parsedTree.char, '{')
  }
}
