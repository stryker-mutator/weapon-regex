package weaponregex.parser

import weaponregex.model.regextree.*

class ParserJVMTest extends munit.FunSuite with ParserTest {
  final val parserFlavor: ParserFlavor = ParserFlavorJVM

  val boundaryMetacharacters: String = """\b\B\A\G\z\Z"""
  val charClassPredefCharClasses: String = """\d\D\h\H\s\S\v\V\w\W"""
  val charClassSpecialChars: String = "(){}.^$|?*+&"
  val escapeCharacters: String = """\\\t\n\r\f\a\e"""
  val hexCharacters: String = "\\x20\\u0020\\x{000020}"
  val octCharacters: String = """\01\012\0123"""
  val predefCharClasses: String = "." + charClassPredefCharClasses

  test("Unparsable: empty positive character class `[]`") {
    val pattern = "[]"
    parseErrorTest(pattern)
  }

  test("Unparsable: empty negative character class `[^]`") {
    val pattern = "[^]"
    parseErrorTest(pattern)
  }

  test("Parse character class with nested character classes") {
    val pattern = "[[a-z][^A-Z0-9][01234]]"
    val parsedTree = Parser(pattern, parserFlavor).get.to[CharacterClass]

    parsedTree.children foreach (child => assert(child.isInstanceOf[CharacterClass], clue = parsedTree.children))

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse character class with simple intersection") {
    val subClasses = Seq("abc", "def", "ghi")
    val pattern = subClasses.mkString("[", "&&", "]")
    val parsedTree = Parser(pattern, parserFlavor).get.to[Node]

    assert(clue(parsedTree).isInstanceOf[CharacterClass])
    assertEquals(parsedTree.children.length, 1)

    val intersection = parsedTree.children.head.to[CharClassIntersection]
    assertEquals(intersection.children.length, 3)
    subClasses zip intersection.children foreach { case (str, child) =>
      assert(clue(child) match {
        case CharacterClassNaked(nodes, _) =>
          str zip nodes forall {
            case (char, Character(c, _)) => c == char
            case _                       => false
          }
        case _ => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse character class with complex intersection") {
    val pattern = """[a-z&&&[a&&b]]"""
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
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
      case _ => false
    })

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
    patterns foreach parseErrorTest
  }

  test("Parse flag toggle group i-i") {
    val pattern = "(?idmsuxU-idmsuxU)"
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
      case FlagToggleGroup(FlagToggle(onFlags, true, offFlags, _), _) =>
        onFlags.flags.map(_.char).mkString == "idmsuxU" && offFlags.flags.map(_.char).mkString == "idmsuxU"
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse flag toggle group i-") {
    val pattern = "(?idmsuxU-)"
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
      case FlagToggleGroup(FlagToggle(onFlags, true, offFlags, _), _) =>
        onFlags.flags.map(_.char).mkString == "idmsuxU" && offFlags.flags.isEmpty
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse flag toggle group -i") {
    val pattern = "(?-idmsuxU)"
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
      case FlagToggleGroup(FlagToggle(onFlags, true, offFlags, _), _) =>
        onFlags.flags.isEmpty && offFlags.flags.map(_.char).mkString == "idmsuxU"
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse flag toggle group -") {
    val pattern = "(?-)"
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
      case FlagToggleGroup(FlagToggle(onFlags, true, offFlags, _), _) =>
        onFlags.flags.isEmpty && offFlags.flags.isEmpty
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse flag toggle group i") {
    val pattern = "(?idmsuxU)"
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
      case FlagToggleGroup(FlagToggle(onFlags, false, offFlags, _), _) =>
        onFlags.flags.map(_.char).mkString == "idmsuxU" && offFlags.flags.isEmpty
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-capturing group with flags i-i") {
    val pattern = "(?idmsux-idmsux:hello)"
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
      case FlagNCGroup(FlagToggle(onFlags, true, offFlags, _), _: Concat, _) =>
        onFlags.flags.map(_.char).mkString == "idmsux" && offFlags.flags.map(_.char).mkString == "idmsux"
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-capturing group with flags i-") {
    val pattern = "(?idmsux-:hello)"
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
      case FlagNCGroup(FlagToggle(onFlags, true, offFlags, _), _: Concat, _) =>
        onFlags.flags.map(_.char).mkString == "idmsux" && offFlags.flags.isEmpty
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-capturing group with flags -i") {
    val pattern = "(?-idmsux:hello)"
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
      case FlagNCGroup(FlagToggle(onFlags, true, offFlags, _), _: Concat, _) =>
        onFlags.flags.isEmpty && offFlags.flags.map(_.char).mkString == "idmsux"
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-capturing group with flags -") {
    val pattern = "(?-:hello)"
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
      case FlagNCGroup(FlagToggle(onFlags, true, offFlags, _), _: Concat, _) =>
        onFlags.flags.isEmpty && offFlags.flags.isEmpty
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-capturing group with flags i") {
    val pattern = "(?idmsux:hello)"
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
      case FlagNCGroup(FlagToggle(onFlags, false, offFlags, _), _: Concat, _) =>
        onFlags.flags.map(_.char).mkString == "idmsux" && offFlags.flags.isEmpty
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse independent non-capturing group") {
    val pattern = "(?>hello)"
    val parsedTree = Parser(pattern, parserFlavor).get

    assert(clue(parsedTree) match {
      case AtomicGroup(_: Concat, _) => true
      case _                         => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse numbered reference") {
    val pattern = """\123"""
    val parsedTree = Parser(pattern, parserFlavor).get.to[NumberReference]

    assertEquals(parsedTree.num, 123)

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse long quote with end") {
    val pattern = """stuff\Q$hit\Emorestuff"""
    val parsedTree = Parser(pattern, parserFlavor).get.to[Concat]

    assert(clue(parsedTree.children(5)) match {
      case Quote("$hit", true, _) => true
      case _                      => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse long quote without end") {
    val pattern = """stuff\Q$hit"""
    val parsedTree = Parser(pattern, parserFlavor).get.to[Concat]

    assert(clue(parsedTree.children(5)) match {
      case Quote("$hit", false, _) => true
      case _                       => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Unparsable: single `{`") {
    val pattern = "{"
    parseErrorTest(pattern)
  }
}
