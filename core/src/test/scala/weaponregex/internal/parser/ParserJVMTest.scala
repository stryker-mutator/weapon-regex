package weaponregex.internal.parser

import weaponregex.internal.extension.EitherExtension.LeftStringEitherTest
import weaponregex.internal.model.regextree.*
import weaponregex.parser.{ParserFlavor, ParserFlavorJVM}

class ParserJVMTest extends munit.FunSuite with ParserTest {
  final val parserFlavor: ParserFlavor = ParserFlavorJVM

  val boundaryMetacharacters: String = """\b\B\A\G\z\Z"""
  val charClassPredefCharClasses: String = """\d\D\h\H\s\S\v\V\w\W"""
  val charClassSpecialChars: String = "(){}.^$|?*+&"
  val escapeCharacters: String = """\\\t\n\r\f\a\e"""
  val hexCharacters: String = "\\x20\\x{000020}\\u0020"
  val octCharacters: String = """\01\012\0123"""
  val predefCharClasses: String = "." + charClassPredefCharClasses

  test("Unparsable: empty positive character class `[]`") {
    val pattern = "[]"
    parseErrorTest(
      pattern,
      """|[]
         |^""".stripMargin
    )
  }

  test("Unparsable: empty negative character class `[^]`") {
    val pattern = "[^]"
    parseErrorTest(
      pattern,
      """|[^]
         |^""".stripMargin
    )
  }

  test("Parse character class with nested character classes") {
    val pattern = "[[a-z][^A-Z0-9][01234]]"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    parsedTree.children foreach (child => assert(child.isInstanceOf[CharacterClass], clue = parsedTree.children))

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse character class with simple intersection") {
    val subClasses = Seq("abc", "def", "ghi")
    val pattern = subClasses.mkString("[", "&&", "]")
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Node]

    assert(clue(parsedTree).isInstanceOf[CharacterClass])
    assertEquals(parsedTree.children.length, 1)

    val intersection = parsedTree.children.head.to[CharClassIntersection]
    assertEquals(intersection.children.length, 3)
    subClasses zip intersection.children foreach { case (str, child) =>
      assertMatches(clue(child)) { case CharacterClassNaked(nodes, _) =>
        str zip nodes forall {
          case (char, Character(c, _)) => c == char
          case _                       => false
        }
      }
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse character class with complex intersection") {
    val pattern = """[a-z&&&[a&&b]]"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) {
      case CharacterClass(
            Seq(
              CharClassIntersection(
                Seq(
                  CharacterClassNaked(
                    Seq(
                      Range(Character('a', _), Character('z', _), _)
                    ),
                    _
                  ),
                  CharacterClassNaked(
                    Seq(
                      Character('&', _),
                      CharacterClass(
                        Seq(
                          CharClassIntersection(
                            Seq(
                              CharacterClassNaked(Seq(Character('a', _)), _),
                              CharacterClassNaked(Seq(Character('b', _)), _)
                            ),
                            _
                          )
                        ),
                        _,
                        true
                      )
                    ),
                    _
                  )
                ),
                _
              )
            ),
            _,
            true
          ) =>
        true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Unparsable: more than 2 consecutive `&` inside character class") {
    val patterns = Seq(
      "[&&&]",
      "[&&&a]",
      "[&&&&]",
      "[&&&&a]",
      "[a&&&&]",
      "[a&&&&a]"
    )
    patterns.foreach(p =>
      parseErrorTest(
        p,
        s"""|$p
            |^""".stripMargin
      )
    )
  }

  test("Unparsable: non-hexadecimal values") {
    val patterns = Seq(
      "\\xGG",
      "\\x{GG}",
      "\\uGGGG"
    )
    patterns.foreach(p =>
      parseErrorTest(
        p,
        s"""|$p
            |  ^""".stripMargin
      )
    )
  }

  test("Unparsable: out-of-range code point hexadecimal values") {
    val pattern = "\\x{110000}" // 10FFFF + 1
    parseErrorTest(
      pattern,
      """|\x{110000}
         |  ^""".stripMargin
    )
  }

  test("Parse character class with Unicode character classes with lone properties") {
    val pattern = """[\p{Alpha}\P{hello_World_0123}]"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    assertMatches(clue(parsedTree.children.head)) { case UnicodeCharClass("Alpha", _, true, None) =>
      true
    }
    assertMatches(clue(parsedTree.children.last)) { case UnicodeCharClass("hello_World_0123", _, false, None) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse character class with Unicode character classes with properties and values") {
    val pattern = """[\p{Script_Extensions=Latin}\P{hello_World_0123=Goodbye_world_321}]"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    assertMatches(clue(parsedTree.children.head)) {
      case UnicodeCharClass("Script_Extensions", _, true, Some("Latin")) => true
    }
    assertMatches(clue(parsedTree.children.last)) {
      case UnicodeCharClass("hello_World_0123", _, false, Some("Goodbye_world_321")) => true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse Unicode character classes with lone properties") {
    val pattern = """\p{Alpha}\P{hello_World_0123}"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assertMatches(clue(parsedTree.children.head)) { case UnicodeCharClass("Alpha", _, true, None) =>
      true
    }
    assertMatches(clue(parsedTree.children.last)) { case UnicodeCharClass("hello_World_0123", _, false, None) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse Unicode character classes with properties and values") {
    val pattern = """\p{Script_Extensions=Latin}\P{hello_World_0123=Goodbye_world_321}"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assertMatches(clue(parsedTree.children.head)) {
      case UnicodeCharClass("Script_Extensions", _, true, Some("Latin")) => true
    }
    assertMatches(clue(parsedTree.children.last)) {
      case UnicodeCharClass("hello_World_0123", _, false, Some("Goodbye_world_321")) => true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse flag toggle group i-i") {
    val pattern = "(?idmsuxU-idmsuxU)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case FlagToggleGroup(FlagToggle(onFlags, true, offFlags, _), _) =>
      onFlags.flags.map(_.char).mkString == "idmsuxU" && offFlags.flags.map(_.char).mkString == "idmsuxU"
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse flag toggle group i-") {
    val pattern = "(?idmsuxU-)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case FlagToggleGroup(FlagToggle(onFlags, true, offFlags, _), _) =>
      onFlags.flags.map(_.char).mkString == "idmsuxU" && offFlags.flags.isEmpty
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse flag toggle group -i") {
    val pattern = "(?-idmsuxU)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case FlagToggleGroup(FlagToggle(onFlags, true, offFlags, _), _) =>
      onFlags.flags.isEmpty && offFlags.flags.map(_.char).mkString == "idmsuxU"
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse flag toggle group -") {
    val pattern = "(?-)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case FlagToggleGroup(FlagToggle(onFlags, true, offFlags, _), _) =>
      onFlags.flags.isEmpty && offFlags.flags.isEmpty
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse flag toggle group i") {
    val pattern = "(?idmsuxU)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case FlagToggleGroup(FlagToggle(onFlags, false, offFlags, _), _) =>
      onFlags.flags.map(_.char).mkString == "idmsuxU" && offFlags.flags.isEmpty
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-capturing group with flags i-i") {
    val pattern = "(?idmsux-idmsux:hello)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case FlagNCGroup(FlagToggle(onFlags, true, offFlags, _), _: Concat, _) =>
      onFlags.flags.map(_.char).mkString == "idmsux" && offFlags.flags.map(_.char).mkString == "idmsux"
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-capturing group with flags i-") {
    val pattern = "(?idmsux-:hello)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case FlagNCGroup(FlagToggle(onFlags, true, offFlags, _), _: Concat, _) =>
      onFlags.flags.map(_.char).mkString == "idmsux" && offFlags.flags.isEmpty
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-capturing group with flags -i") {
    val pattern = "(?-idmsux:hello)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case FlagNCGroup(FlagToggle(onFlags, true, offFlags, _), _: Concat, _) =>
      onFlags.flags.isEmpty && offFlags.flags.map(_.char).mkString == "idmsux"
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-capturing group with flags -") {
    val pattern = "(?-:hello)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case FlagNCGroup(FlagToggle(onFlags, true, offFlags, _), _: Concat, _) =>
      onFlags.flags.isEmpty && offFlags.flags.isEmpty
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-capturing group with flags i") {
    val pattern = "(?idmsux:hello)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case FlagNCGroup(FlagToggle(onFlags, false, offFlags, _), _: Concat, _) =>
      onFlags.flags.map(_.char).mkString == "idmsux" && offFlags.flags.isEmpty
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse independent non-capturing group") {
    val pattern = "(?>hello)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assertMatches(clue(parsedTree)) { case AtomicGroup(_: Concat, _) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse numbered reference") {
    val pattern = """\123"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[NumberReference]

    assertEquals(parsedTree.num, 123)

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse long quote with end") {
    val pattern = """stuff\Q$hit\Emorestuff"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assertMatches(clue(parsedTree.children(5))) { case Quote("$hit", true, _) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse long quote without end") {
    val pattern = """stuff\Q$hit"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assertMatches(clue(parsedTree.children(5))) { case Quote("$hit", false, _) =>
      true
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Unparsable: single `{`") {
    val pattern = "{"
    parseErrorTest(
      pattern,
      """|{
         |^""".stripMargin
    )
  }

  test("adds context to parse error") {
    val pattern = "{"
    parseErrorTest(
      pattern,
      """|{
         |^""".stripMargin,
      context = Seq(
        "unicode character",
        "code point character",
        "unicode character class",
        "flag toggle group",
        "atomic group",
        "long quote"
      )
    )
  }

  test("Unparsable: JVM flavor with String flags") {
    val pattern = "abc"
    parseErrorTest(
      pattern,
      """|[Error] Parser: JVM regex flavor does not support string flags""".stripMargin,
      Some("u")
    )
  }
}
