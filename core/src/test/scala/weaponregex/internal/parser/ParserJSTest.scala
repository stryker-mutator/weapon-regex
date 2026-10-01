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
  val unicodeFlag: Option[String] = Some("u")

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

    assertMatches(clue(parsedTree.children.head)) { case Character('[', _) =>
      true
    }

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

    assertMatches(clue(parsedTree.children.head)) { case QuoteChar('u', _) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\u{20}` as quantifier without the Unicode flag") {
    val pattern = "\\u{20}"
    val parsedTree = Parser(pattern, None, parserFlavor).getOrFail.to[Quantifier]

    assertMatches(clue(parsedTree)) { case Quantifier(QuoteChar('u', _), 20, 20, _, GreedyQuantifier, true) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\x{20}` as quantifier without the Unicode flag") {
    val pattern = "\\x{20}"
    val parsedTree = Parser(pattern, None, parserFlavor).getOrFail.to[Quantifier]

    assertMatches(clue(parsedTree)) { case Quantifier(QuoteChar('x', _), 20, 20, _, GreedyQuantifier, true) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\u{FFFFFF}` as character quotation without the Unicode flag") {
    val pattern = "\\u{FFFFFF}"
    val parsedTree = Parser(pattern, None, parserFlavor).getOrFail.to[Concat]

    assertMatches(clue(parsedTree.children.head)) { case QuoteChar('u', _) =>
      true
    }

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
    parseErrorTest(
      pattern,
      """|\x{20}
         | ^
         |expectations:
         |* must be char: '$'
         |* must be a char within the range of: ['(', '+']
         |* must be a char within the range of: ['.', '/']
         |* must be char: '?'
         |* must be a char within the range of: ['[', '^']
         |* must be a char within the range of: ['{', '}']""".stripMargin,
      Some("u")
    )
    parseErrorTest(
      pattern,
      """|\x{20}
         | ^
         |expectations:
         |* must be char: '$'
         |* must be a char within the range of: ['(', '+']
         |* must be a char within the range of: ['.', '/']
         |* must be char: '?'
         |* must be a char within the range of: ['[', '^']
         |* must be a char within the range of: ['{', '}']""".stripMargin,
      Some("v")
    )
  }

  test("Unparsable: `\\u20` with the Unicode flag") {
    val dollar = '$'
    val pattern = "\\u20"
    parseErrorTest(
      pattern,
      s"""|\\u20
          | ^
          |expectations:
          |* must be char: '$dollar'
          |* must be a char within the range of: ['(', '+']
          |* must be a char within the range of: ['.', '/']
          |* must be char: '?'
          |* must be a char within the range of: ['[', '^']
          |* must be a char within the range of: ['{', '}']""".stripMargin,
      Some("u")
    )
    parseErrorTest(
      pattern,
      s"""|\\u20
          | ^
          |expectations:
          |* must be char: '$dollar'
          |* must be a char within the range of: ['(', '+']
          |* must be a char within the range of: ['.', '/']
          |* must be char: '?'
          |* must be a char within the range of: ['[', '^']
          |* must be a char within the range of: ['{', '}']""".stripMargin,
      Some("v")
    )
  }

  test("Unparsable: out-of-range code point hexadecimal values with the Unicode flag") {
    val dollar = '$'
    // 10FFFF + 1, and a value that overflows an Int
    for {
      pattern <- Seq("\\u{110000}", "\\u{FFFFFFFFFF}")
      flag <- Seq("u", "v")
    } parseErrorTest(
      pattern,
      s"""|$pattern
          | ^
          |expectations:
          |* must be char: '$dollar'
          |* must be a char within the range of: ['(', '+']
          |* must be a char within the range of: ['.', '/']
          |* must be char: '?'
          |* must be a char within the range of: ['[', '^']
          |* must be a char within the range of: ['{', '}']""".stripMargin,
      Some(flag)
    )
  }

  test("Parse character class with Unicode character classes with lone properties, with the Unicode flag") {
    val pattern = """[\p{Alpha}\P{hello_World_0123}]"""

    Seq("u", "v").foreach(flag => {
      val parsedTree = Parser(pattern, Some(flag), parserFlavor).getOrFail.to[CharacterClass]

      assertMatches(clue(parsedTree.children.head)) { case UnicodeCharClass("Alpha", _, true, None) =>
        true
      }
      assertMatches(clue(parsedTree.children.last)) { case UnicodeCharClass("hello_World_0123", _, false, None) =>
        true
      }

      treeBuildTest(parsedTree, pattern)
    })
  }

  test("Parse character class with Unicode character classes with properties and values, with the Unicode flag") {
    val pattern = """[\p{Script_Extensions=Latin}\P{hello_World_0123=Goodbye_world_321}]"""

    Seq("u", "v").foreach(flag => {
      val parsedTree = Parser(pattern, Some(flag), parserFlavor).getOrFail.to[CharacterClass]

      assertMatches(clue(parsedTree.children.head)) {
        case UnicodeCharClass("Script_Extensions", _, true, Some("Latin")) => true
      }
      assertMatches(clue(parsedTree.children.last)) {
        case UnicodeCharClass("hello_World_0123", _, false, Some("Goodbye_world_321")) => true
      }

      treeBuildTest(parsedTree, pattern)
    })
  }

  test("Parse Unicode character classes with lone properties, with the Unicode flag") {
    val pattern = """\p{Alpha}\P{hello_World_0123}"""

    Seq("u", "v").foreach(flag => {
      val parsedTree = Parser(pattern, Some(flag), parserFlavor).getOrFail.to[Concat]

      assertMatches(clue(parsedTree.children.head)) { case UnicodeCharClass("Alpha", _, true, None) =>
        true
      }
      assertMatches(clue(parsedTree.children.last)) { case UnicodeCharClass("hello_World_0123", _, false, None) =>
        true
      }

      treeBuildTest(parsedTree, pattern)
    })
  }

  test("Parse Unicode character classes with properties and values, with the Unicode flag") {
    val pattern = """\p{Script_Extensions=Latin}\P{hello_World_0123=Goodbye_world_321}"""

    Seq("u", "v").foreach(flag => {
      val parsedTree = Parser(pattern, Some(flag), parserFlavor).getOrFail.to[Concat]

      assertMatches(clue(parsedTree.children.head)) {
        case UnicodeCharClass("Script_Extensions", _, true, Some("Latin")) => true
      }
      assertMatches(clue(parsedTree.children.last)) {
        case UnicodeCharClass("hello_World_0123", _, false, Some("Goodbye_world_321")) => true
      }

      treeBuildTest(parsedTree, pattern)
    })
  }

  test("Parse `\\p{Alpha}` as a character quotation in a character class without the Unicode flag") {
    val pattern = """[\p{Alpha}]"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    assertMatches(clue(parsedTree.children.head)) { case QuoteChar('p', _) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\p{Script_Extensions=Latin}` as a character quotation in a character class without the Unicode flag") {
    val pattern = """[\p{Script_Extensions=Latin}]"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    assertMatches(clue(parsedTree.children.head)) { case QuoteChar('p', _) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\p{Alpha}` as a character quotation without the Unicode flag") {
    val pattern = """\p{Alpha}"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assertMatches(clue(parsedTree.children.head)) { case QuoteChar('p', _) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `\\p{Script_Extensions=Latin}` as a character quotation without the Unicode flag") {
    val pattern = """\p{Script_Extensions=Latin}"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assertMatches(clue(parsedTree.children.head)) { case QuoteChar('p', _) =>
      true
    }

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

    assertMatches(clue(parsedTree.children.head)) { case NamedGroup(_: Concat, name, _) =>
      name == "group_Name_1"
    }
    assertMatches(clue(parsedTree.children.last)) { case NamedGroup(_: Concat, name, _) =>
      name == "Group_Name_2"
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse nested named capturing group with underscores in name") {
    val pattern = "(?<group_Name_1>hello(?<Group_Name_2>world))"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case NamedGroup(Concat(nodes, _), "group_Name_1", _) =>
      assertMatches(clue(nodes.last)) { case NamedGroup(_: Concat, "Group_Name_2", _) =>
        true
      }
      true

    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse named reference with underscores in name") {
    val pattern = """\k<name_1>"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[NameReference]

    assertEquals(parsedTree.name, "name_1")

    treeBuildTest(parsedTree, pattern)
  }

  // `$`, non-ASCII letters, a supplementary-plane letter (U+1D49C, a surrogate pair), ZWNJ and ZWJ
  val identifierNames: Seq[String] = Seq("$t", "a$", "caf\u00e9", "\u540d\u524d", "\ud835\udc9c1", "a\u200cb\u200dc") ++
    // Unicode escapes: `\ uXXXX` (upper- and lowercase hex), `\ u{X...}`, and an escaped surrogate pair (U+1D49C)
    Seq("\\u0061b", "a\\u0062", "caf\\u00E9", "\\u0024", "\\u{61}", "\\u{0000061}", "\\u{1d49c}", "\\ud835\\udc9c")

  test("Parse named capturing group with ECMAScript identifier names in Unicode mode") {
    identifierNames foreach { name =>
      val pattern = s"(?<$name>hello)\\k<$name>"
      val parsedTree = Parser(pattern, unicodeFlag, parserFlavor).getOrFail.to[Concat]

      assertMatches(clue(parsedTree.children.head)) { case NamedGroup(_, `name`, _) => true }
      assertMatches(clue(parsedTree.children.last)) { case NameReference(`name`, _) => true }

      treeBuildTest(parsedTree, pattern)
    }
  }

  test("Parse named capturing group with ECMAScript identifier names") {
    identifierNames foreach { name =>
      val pattern = s"(?<$name>hello)"
      val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[NamedGroup]

      assertEquals(parsedTree.name, name)

      treeBuildTest(parsedTree, pattern)
    }
  }

  test("Parse named reference with ECMAScript identifier names") {
    identifierNames foreach { name =>
      val pattern = s"\\k<$name>"
      val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[NameReference]

      assertEquals(parsedTree.name, name)

      treeBuildTest(parsedTree, pattern)
    }
  }

  test("Unparsable: named capturing group name starting with a non-start character") {
    // A digit, a combining mark (U+0301) and ZWNJ may continue a name, but not start one
    Seq("(?<1a>hello)", "(?<\u0301a>hello)", "(?<\u200ca>hello)") foreach { pattern =>
      parseErrorTest(
        pattern,
        s"""|$pattern
            |^""".stripMargin
      )
    }
  }

  test("Unparsable: named capturing group name with an invalid Unicode escape") {
    Seq(
      "\\u0031a", // escaped digit cannot start a name
      "\\u{110000}", // beyond the maximum code point
      "\\u{FFFFFFFFFF}", // overflows an Int
      "\\ud835", // lone high surrogate
      "a\\udc9c", // lone low surrogate
      "\\u{}",
      "\\u00",
      "\\x61",
      "\\U0061"
    ) foreach { name =>
      val pattern = s"(?<$name>hello)"
      parseErrorTest(
        pattern,
        s"""|$pattern
            |^""".stripMargin
      )
    }
  }

  test("Unparsable: flag toggle group i-i") {
    val pattern = "(?idmsuxU-idmsuxU)"
    parseErrorTest(
      pattern,
      """|(?idmsuxU-idmsuxU)
         |^""".stripMargin
    )
  }

  test("Unparsable: flag toggle group i-") {
    val pattern = "(?idmsuxU-)"
    parseErrorTest(
      pattern,
      """|(?idmsuxU-)
         |^""".stripMargin
    )
  }

  test("Unparsable: flag toggle group -i") {
    val pattern = "(?-idmsuxU)"
    parseErrorTest(
      pattern,
      """|(?-idmsuxU)
         |^""".stripMargin
    )
  }

  test("Unparsable: flag toggle group -") {
    val pattern = "(?-)"
    parseErrorTest(
      pattern,
      """|(?-)
         |^""".stripMargin
    )
  }

  test("Unparsable: flag toggle group i") {
    val pattern = "(?idmsuxU)"
    parseErrorTest(
      pattern,
      """|(?idmsuxU)
         |^""".stripMargin
    )
  }

  test("Unparsable: non-capturing group with flags i-i") {
    val pattern = "(?idmsux-idmsux:hello)"
    parseErrorTest(
      pattern,
      """|(?idmsux-idmsux:hello)
         |^""".stripMargin
    )
  }

  test("Unparsable: non-capturing group with flags i-") {
    val pattern = "(?idmsux-:hello)"
    parseErrorTest(
      pattern,
      """|(?idmsux-:hello)
         |^""".stripMargin
    )
  }

  test("Unparsable: non-capturing group with flags -i") {
    val pattern = "(?-idmsux:hello)"
    parseErrorTest(
      pattern,
      """|(?-idmsux:hello)
         |^""".stripMargin
    )
  }

  test("Unparsable: non-capturing group with flags -") {
    val pattern = "(?-:hello)"
    parseErrorTest(
      pattern,
      """|(?-:hello)
         |^""".stripMargin
    )
  }

  test("Unparsable: non-capturing group with flags i") {
    val pattern = "(?idmsux:hello)"
    parseErrorTest(
      pattern,
      """|(?idmsux:hello)
         |^""".stripMargin
    )
  }

  test("Unparsable: independent non-capturing group") {
    val pattern = "(?>hello)"
    parseErrorTest(
      pattern,
      """|(?>hello)
         |^""".stripMargin
    )
  }

  test("""Parse `\Q\E` as character quotes""") {
    val pattern = """stuff\Q$hit\Emorestuff"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assertMatches(clue(parsedTree.children(5))) { case QuoteChar('Q', _) =>
      true
    }
    assertMatches(clue(parsedTree.children(10))) { case QuoteChar('E', _) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("""Parse `\Q` as a character quote""") {
    val pattern = """stuff\Q$hit"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assertMatches(clue(parsedTree.children(5))) { case QuoteChar('Q', _) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse syntax characters escape with the Unicode flag") {
    val syntaxChars = """^$\.*+?()[]{}|/"""
    val pattern = "\\" + syntaxChars.mkString("\\")

    Seq("u", "v").foreach(flag => {
      val parsedTree = Parser(pattern, Some(flag), parserFlavor).getOrFail.to[Concat]

      syntaxChars zip parsedTree.children foreach { case (char, child) =>
        assertMatches(clue(child)) {
          case MetaChar(c, _)  => c.head == char
          case QuoteChar(c, _) => c == char
        }
      }

      treeBuildTest(parsedTree, pattern)
    })
  }

  test("Unparsable: non-syntax character escape with the Unicode flag") {
    val pattern = "\\a"
    parseErrorTest(
      pattern,
      """|\a
         | ^""".stripMargin,
      Some("u")
    )
    parseErrorTest(
      pattern,
      """|\a
         | ^""".stripMargin,
      Some("v")
    )
  }

  test("Unparsable: long-quantifier-like with nothing preceding") {
    val patterns = Seq(
      "{1}" -> """|{1}
                  |^""".stripMargin,
      "{1,}" -> """|{1,}
                   |^""".stripMargin,
      "{1,2}" -> """|{1,2}
                    |^""".stripMargin
    )
    patterns.foreach { case (p, m) => parseErrorTest(p, m) }
  }

  test("Parse `{`") {
    val pattern = "{"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Character]

    assertEquals(parsedTree.char, '{')
  }
}
