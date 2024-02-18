package weaponregex.internal.parser

import weaponregex.internal.extension.EitherExtension.LeftStringEitherTest
import weaponregex.internal.model.regextree.*
import weaponregex.parser.{ParserFlavor, ParserFlavorJS}

class ParserJSTest extends munit.FunSuite with ParserTest {
  final val parserFlavor: ParserFlavor = ParserFlavorJS

  val boundaryMetacharacters: String = """\b\B"""
  val charClassPredefCharClasses: String = """\d\D\s\S\v\w\W"""
  val charClassSpecialChars: String = "(){}.^$|?*+"
  val escapeCharacters: String = """\\\t\n\r\f"""
  val hexCharacters: String = "\\x20\\x21"
  val octCharacters: String = """\1\12\123"""
  val predefCharClasses: String = "." + charClassPredefCharClasses

  test("""Not parse `\A\G\z\Z` as boundary metacharacters""") {
    val pattern = """\A\G\z\Z"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Node]

    parsedTree.children foreach (child => assert(!clue(child).isInstanceOf[Boundary]))

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse empty positive character class with characters") {
    val pattern = "[]"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    assert(clue(parsedTree.children).isEmpty)

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse negative character class with characters") {
    val pattern = "[^]"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    assert(!parsedTree.isPositive)
    assert(clue(parsedTree.children).isEmpty)

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `[` as character inside a character class") {
    val pattern = "[[]"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    assert(clue(parsedTree.children.head) match {
      case Character('[', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("""Not parse `\a\e` as escape characters""") {
    val pattern = """\a\e"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    parsedTree.children foreach (child => assert(!clue(child).isInstanceOf[MetaChar]))

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-hexadecimal value `\\xGG` without the Unicode flag") {
    val pattern = "\\xGG"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assertEquals(parsedTree.children.length, 3)

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-unicode value `\\uGGGG` without the Unicode flag") {
    val pattern = "\\uGGGG"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assertEquals(parsedTree.children.length, 5)

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\u20` as character quotation without the Unicode flag") {
    val pattern = "\\u20"
    val parsedTree = Parser(pattern, None, parserFlavor).getOrFail.to[Concat]

    assert(clue(parsedTree.children.head) match {
      case QuoteChar('u', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\u{20}` as quantifier without the Unicode flag") {
    val pattern = "\\u{20}"
    val parsedTree = Parser(pattern, None, parserFlavor).getOrFail.to[Quantifier]

    assert(clue(parsedTree) match {
      case Quantifier(QuoteChar('u', _), 20, 20, _, GreedyQuantifier, true) => true
      case _                                                                => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\x{20}` as quantifier without the Unicode flag") {
    val pattern = "\\x{20}"
    val parsedTree = Parser(pattern, None, parserFlavor).getOrFail.to[Quantifier]

    assert(clue(parsedTree) match {
      case Quantifier(QuoteChar('x', _), 20, 20, _, GreedyQuantifier, true) => true
      case _                                                                => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\u{FFFFFF}` as character quotation without the Unicode flag") {
    val pattern = "\\u{FFFFFF}"
    val parsedTree = Parser(pattern, None, parserFlavor).getOrFail.to[Concat]

    assert(clue(parsedTree.children.head) match {
      case QuoteChar('u', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\u{20}` as Unicode character with the Unicode flag") {
    val pattern = "\\u{20}"

    Seq("u", "v").foreach(flag => {
      val parsedTree = Parser(pattern, Some(flag + "g"), parserFlavor).getOrFail.to[MetaChar]

      assertEquals(parsedTree.metaChar, "u{20}")

      treeBuildTest(parsedTree, pattern)
    })
  }

  test("Unparsable: `\\x{20}` with the Unicode flag") {
    val pattern = "\\x{20}"
    parseErrorTest(pattern, Some("u"))
    parseErrorTest(pattern, Some("v"))
  }

  test("Unparsable: `\\u20` with the Unicode flag") {
    val pattern = "\\u20"
    parseErrorTest(pattern, Some("u"))
    parseErrorTest(pattern, Some("v"))
  }

  test("Unparsable: out-of-range code point hexadecimal values with the Unicode flag") {
    val pattern = "\\u{110000}" // 10FFFF + 1
    parseErrorTest(pattern, Some("u"))
    parseErrorTest(pattern, Some("v"))
  }

  test("Parse character class with Unicode character classes with lone properties, with the Unicode flag") {
    val pattern = """[\p{Alpha}\P{hello_World_0123}]"""

    Seq("u", "v").foreach(flag => {
      val parsedTree = Parser(pattern, Some(flag), parserFlavor).getOrFail.to[CharacterClass]

      assert(clue(parsedTree.children.head) match {
        case UnicodeCharClass("Alpha", _, true, None) => true
        case _                                        => false
      })
      assert(clue(parsedTree.children.last) match {
        case UnicodeCharClass("hello_World_0123", _, false, None) => true
        case _                                                    => false
      })

      treeBuildTest(parsedTree, pattern)
    })
  }

  test("Parse character class with Unicode character classes with properties and values, with the Unicode flag") {
    val pattern = """[\p{Script_Extensions=Latin}\P{hello_World_0123=Goodbye_world_321}]"""

    Seq("u", "v").foreach(flag => {
      val parsedTree = Parser(pattern, Some(flag), parserFlavor).getOrFail.to[CharacterClass]

      assert(clue(parsedTree.children.head) match {
        case UnicodeCharClass("Script_Extensions", _, true, Some("Latin")) => true
        case _                                                             => false
      })
      assert(clue(parsedTree.children.last) match {
        case UnicodeCharClass("hello_World_0123", _, false, Some("Goodbye_world_321")) => true
        case _                                                                         => false
      })

      treeBuildTest(parsedTree, pattern)
    })
  }

  test("Parse Unicode character classes with lone properties, with the Unicode flag") {
    val pattern = """\p{Alpha}\P{hello_World_0123}"""

    Seq("u", "v").foreach(flag => {
      val parsedTree = Parser(pattern, Some(flag), parserFlavor).getOrFail.to[Concat]

      assert(clue(parsedTree.children.head) match {
        case UnicodeCharClass("Alpha", _, true, None) => true
        case _                                        => false
      })
      assert(clue(parsedTree.children.last) match {
        case UnicodeCharClass("hello_World_0123", _, false, None) => true
        case _                                                    => false
      })

      treeBuildTest(parsedTree, pattern)
    })
  }

  test("Parse Unicode character classes with properties and values, with the Unicode flag") {
    val pattern = """\p{Script_Extensions=Latin}\P{hello_World_0123=Goodbye_world_321}"""

    Seq("u", "v").foreach(flag => {
      val parsedTree = Parser(pattern, Some(flag), parserFlavor).getOrFail.to[Concat]

      assert(clue(parsedTree.children.head) match {
        case UnicodeCharClass("Script_Extensions", _, true, Some("Latin")) => true
        case _                                                             => false
      })
      assert(clue(parsedTree.children.last) match {
        case UnicodeCharClass("hello_World_0123", _, false, Some("Goodbye_world_321")) => true
        case _                                                                         => false
      })

      treeBuildTest(parsedTree, pattern)
    })
  }

  test("Parse `\\p{Alpha}` as a character quotation in a character class without the Unicode flag") {
    val pattern = """[\p{Alpha}]"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    assert(clue(parsedTree.children.head) match {
      case QuoteChar('p', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\p{Script_Extensions=Latin}` as a character quotation in a character class without the Unicode flag") {
    val pattern = """[\p{Script_Extensions=Latin}]"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    assert(clue(parsedTree.children.head) match {
      case QuoteChar('p', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\p{Alpha}` as a character quotation without the Unicode flag") {
    val pattern = """\p{Alpha}"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(clue(parsedTree.children.head) match {
      case QuoteChar('p', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\p{Script_Extensions=Latin}` as a character quotation without the Unicode flag") {
    val pattern = """\p{Script_Extensions=Latin}"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(clue(parsedTree.children.head) match {
      case QuoteChar('p', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("""Not parse `\h\H\V` as predefined character class""") {
    val pattern = """\h\H\V"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    parsedTree.children foreach (child => assert(!clue(child).isInstanceOf[PredefinedCharClass]))

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse named capturing group with underscores in name") {
    val pattern = "(?<group_Name_1>hello)(?<Group_Name_2>world)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(clue(parsedTree.children.head) match {
      case NamedGroup(_: Concat, name, _) => name == "group_Name_1"
      case _                              => false
    })
    assert(clue(parsedTree.children.last) match {
      case NamedGroup(_: Concat, name, _) => name == "Group_Name_2"
      case _                              => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse nested named capturing group with underscores in name") {
    val pattern = "(?<group_Name_1>hello(?<Group_Name_2>world))"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assert(clue(parsedTree) match {
      case NamedGroup(Concat(nodes, _), "group_Name_1", _) =>
        assert(clue(nodes.last) match {
          case NamedGroup(_: Concat, "Group_Name_2", _) => true
          case _                                        => false
        })
        true
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse named reference with underscores in name") {
    val pattern = """\k<name_1>"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[NameReference]

    assertEquals(parsedTree.name, "name_1")

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
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

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
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(clue(parsedTree.children(5)) match {
      case QuoteChar('Q', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse syntax characters escape with the Unicode flag") {
    val syntaxChars = """^$\.*+?()[]{}|/"""
    val pattern = "\\" + syntaxChars.mkString("\\")

    Seq("u", "v").foreach(flag => {
      val parsedTree = Parser(pattern, Some(flag), parserFlavor).getOrFail.to[Concat]

      syntaxChars zip parsedTree.children foreach { case (char, child) =>
        assert(clue(child) match {
          case MetaChar(c, _)  => c.head == char
          case QuoteChar(c, _) => c == char
          case _               => false
        })
      }

      treeBuildTest(parsedTree, pattern)
    })
  }

  test("Unparsable: non-syntax character escape with the Unicode flag") {
    val pattern = "\\a"
    parseErrorTest(pattern, Some("u"))
    parseErrorTest(pattern, Some("v"))
  }

  test("Unparsable: long-quantifier-like with nothing preceding") {
    val patterns = Seq(
      "{1}",
      "{1,}",
      "{1,2}"
    )
    patterns.foreach(parseErrorTest(_))
  }

  test("Parse `{`") {
    val pattern = "{"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Character]

    assertEquals(parsedTree.char, '{')
  }
}
