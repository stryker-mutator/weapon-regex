package weaponregex.parser

import munit.Location
import weaponregex.constant.ErrorMessage
import weaponregex.extension.EitherExtension.LeftStringEitherTest
import weaponregex.extension.RegexTreeExtension.RegexTreeStringBuilder
import weaponregex.model.regextree.*

import scala.reflect.ClassTag

trait ParserTest {
  this: munit.FunSuite =>

  val parserFlavor: ParserFlavor

  val boundaryMetacharacters: String
  val charClassPredefCharClasses: String
  val charClassSpecialChars: String
  val escapeCharacters: String
  val hexCharacters: String
  val octCharacters: String
  val predefCharClasses: String

  def treeBuildTest(tree: RegexTree, pattern: String): Unit = assertEquals(tree.build, pattern)

  def parseErrorTest(pattern: String, flags: Option[String] = None): Unit = {
    val parsedTree = Parser(pattern, flags, parserFlavor)

    assert(clue(parsedTree) match {
      case Left(msg) => msg.startsWith(ErrorMessage.parserErrorHeader)
      case _         => false
    })
  }

  test("Parse concat of characters") {
    val pattern = "hello"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    pattern zip parsedTree.children foreach { case (char, child) =>
      assert(clue(child) match {
        case Character(c, _) => c == char
        case _               => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `}` character next to long quantifier") {
    val pattern = "a{1}}"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(parsedTree.children.head.isInstanceOf[Quantifier])
    assert(parsedTree.children.last match {
      case Character('}', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse `]` character next to character class") {
    val pattern = "[abc]]"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(parsedTree.children.head.isInstanceOf[CharacterClass])
    assert(parsedTree.children.last match {
      case Character(']', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse or of characters") {
    val pattern = "h|e|l|l|o"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Or]

    pattern.replace("|", "") zip parsedTree.children foreach { case (char, child) =>
      assert(clue(child) match {
        case Character(c, _) => c == char
        case _               => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse or of characters with null children") {
    val pattern = "|h|e||l|||l|o|"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Or]

    assert(clue(parsedTree.children) match {
      case Seq(
            Empty(_),
            Character('h', _),
            Character('e', _),
            Empty(_),
            Character('l', _),
            Empty(_),
            Empty(_),
            Character('l', _),
            Character('o', _),
            Empty(_)
          ) =>
        true
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse BOL and EOL") {
    val pattern = "^hello$"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]
    assert(clue(parsedTree.children.head).isInstanceOf[BOL])
    assert(clue(parsedTree.children.last).isInstanceOf[EOL])

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse boundary metacharacters") {
    val pattern = boundaryMetacharacters
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    pattern.replace("""\""", "") zip parsedTree.children foreach { case (char, child) =>
      assert(clue(child) match {
        case Boundary(str, _) => str.head == char
        case _                => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse multiple lines with location") {
    val pattern =
      """a
        |a
        |a
        |a
        |a
        |a""".stripMargin
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    parsedTree.children filter {
      case Character('a', _) => true
      case _                 => false
    } zip (0 to 5) foreach { case (node, n) =>
      assert(clue(node).location.start.line == n)
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse positive character class with characters") {
    val pattern = "[abc]"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    pattern.init.tail zip parsedTree.children foreach { case (char, child) =>
      assert(clue(child) match {
        case Character(c, _) => c == char
        case _               => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse negative character class with characters") {
    val pattern = "[^abc]"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Node]

    assert(parsedTree match {
      case CharacterClass(_, _, false) => true
      case _                           => false
    })
    pattern.drop(2).init zip parsedTree.children foreach { case (char, child) =>
      assert(clue(child) match {
        case Character(c, _) => c == char
        case _               => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse character class with ranges") {
    // Used for easier assertions
    // includes unusual range $-%
    val ranges = Seq("az", "AZ", "09", "$%")
    // Generate pattern from these ranges
    val pattern = "[" + ranges.map(r => s"${r.head}-${r.last}").mkString + "]"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    ranges zip parsedTree.children foreach { case (range, child) =>
      assert(clue(child) match {
        case Range(Character(l, _), Character(r, _), _) => l == range.head && r == range.last
        case _                                          => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse character class with predefined character classes") {
    val pattern = s"[$charClassPredefCharClasses]"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    pattern.init.tail.replace("""\""", "") zip parsedTree.children foreach { case (char, child) =>
      assert(clue(child) match {
        case PredefinedCharClass(charClass, _) => charClass.head == char
        case _                                 => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse character class with quotes") {
    val pattern = """[\]]"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assert(clue(parsedTree) match {
      case CharacterClass(nodes, _, true) => nodes.head.isInstanceOf[QuoteChar]
      case _                              => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse character class with metacharacters") {
    val pattern = """[\\\t\n\r\f]"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    // A backslash is added back in to represent the backslash in the pattern
    """\""" + pattern.tail.init.replace("""\""", "") zip parsedTree.children foreach { case (char, child) =>
      assert(clue(child) match {
        case MetaChar(metaChar, _) => metaChar.head == char
        case _                     => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse character class with special characters") {
    val pattern = s"[$charClassSpecialChars]"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[CharacterClass]

    pattern.tail.init zip parsedTree.children foreach { case (char, child) =>
      assert(clue(child) match {
        case Character(c, _) => c == char
        case _               => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse escape characters") {
    val pattern = escapeCharacters
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    // A backslash is added back in to represent the backslash in the pattern
    """\""" + pattern.replace("""\""", "") zip parsedTree.children foreach { case (char, child) =>
      assert(clue(child) match {
        case MetaChar(metaChar, _) => metaChar.head == char
        case _                     => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse control characters") {
    val controlChars: Seq[Char] = ('a' to 'z') ++ ('A' to 'Z')
    val pattern = (controlChars map ("""\c""" + _)).mkString
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    controlChars zip parsedTree.children foreach { case (char, child) =>
      assert(clue(child) match {
        case ControlChar(controlChar, _) => controlChar.head == char
        case _                           => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse hexadecimal characters") {
    val pattern = hexCharacters
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    pattern.split("""\\""").tail zip parsedTree.children foreach { case (str, child) =>
      assert(clue(child) match {
        case MetaChar(metaChar, _) => metaChar == str
        case _                     => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse octal characters") {
    val pattern = octCharacters
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    pattern.split("""\\""").tail zip parsedTree.children foreach { case (str, child) =>
      assert(clue(child) match {
        case MetaChar(metaChar, _) => metaChar == str
        case _                     => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse predefined character class") {
    val pattern = predefCharClasses
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    pattern.replace("""\""", "") zip parsedTree.children foreach { case (char, child) =>
      assert(clue(child) match {
        case _: AnyDot                         => char == '.'
        case PredefinedCharClass(charClass, _) => charClass.head == char
        case _                                 => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse short greedy quantifiers") {
    val pattern = "a*b+c?"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(clue(parsedTree.children(0)).isInstanceOf[ZeroOrMore])
    assert(clue(parsedTree.children(1)).isInstanceOf[OneOrMore])
    assert(clue(parsedTree.children(2)).isInstanceOf[ZeroOrOne])

    pattern.split("""[*?+]""") zip parsedTree.children foreach { case (str, child) =>
      assert(clue(child) match {
        case ZeroOrMore(Character(c, _), _, t) => c == str.head && t == GreedyQuantifier
        case OneOrMore(Character(c, _), _, t)  => c == str.head && t == GreedyQuantifier
        case ZeroOrOne(Character(c, _), _, t)  => c == str.head && t == GreedyQuantifier
        case _                                 => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse short reluctant quantifiers") {
    val pattern = "a*?b+?c??"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(clue(parsedTree.children(0)).isInstanceOf[ZeroOrMore])
    assert(clue(parsedTree.children(1)).isInstanceOf[OneOrMore])
    assert(clue(parsedTree.children(2)).isInstanceOf[ZeroOrOne])

    pattern.split("""[*?+]+""") zip parsedTree.children foreach { case (str, child) =>
      assert(clue(child) match {
        case ZeroOrMore(Character(c, _), _, t) => c == str.head && t == ReluctantQuantifier
        case OneOrMore(Character(c, _), _, t)  => c == str.head && t == ReluctantQuantifier
        case ZeroOrOne(Character(c, _), _, t)  => c == str.head && t == ReluctantQuantifier
        case _                                 => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse short possessive quantifiers") {
    val pattern = "a*+b++c?+"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(clue(parsedTree.children(0)).isInstanceOf[ZeroOrMore])
    assert(clue(parsedTree.children(1)).isInstanceOf[OneOrMore])
    assert(clue(parsedTree.children(2)).isInstanceOf[ZeroOrOne])

    pattern.split("""[*?+]+""") zip parsedTree.children foreach { case (str, child) =>
      assert(clue(child) match {
        case ZeroOrMore(Character(c, _), _, t) => c == str.head && t == PossessiveQuantifier
        case OneOrMore(Character(c, _), _, t)  => c == str.head && t == PossessiveQuantifier
        case ZeroOrOne(Character(c, _), _, t)  => c == str.head && t == PossessiveQuantifier
        case _                                 => false
      })
    }

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse long greedy quantifiers") {
    val pattern = "a{1}b{1,}c{1,2}"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(parsedTree.children(0) match {
      case Quantifier(Character('a', _), 1, 1, _, GreedyQuantifier, true) => true
      case _                                                              => false
    })

    assert(parsedTree.children(1) match {
      case Quantifier(Character('b', _), 1, Quantifier.Infinity, _, GreedyQuantifier, false) => true
      case _                                                                                 => false
    })

    assert(parsedTree.children(2) match {
      case Quantifier(Character('c', _), 1, 2, _, GreedyQuantifier, false) => true
      case _                                                               => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse long reluctant quantifiers") {
    val pattern = "a{1}?b{1,}?c{1,2}?"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(parsedTree.children(0) match {
      case Quantifier(Character('a', _), 1, 1, _, ReluctantQuantifier, true) => true
      case _                                                                 => false
    })

    assert(parsedTree.children(1) match {
      case Quantifier(Character('b', _), 1, Quantifier.Infinity, _, ReluctantQuantifier, false) => true
      case _                                                                                    => false
    })

    assert(parsedTree.children(2) match {
      case Quantifier(Character('c', _), 1, 2, _, ReluctantQuantifier, false) => true
      case _                                                                  => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse long possessive quantifiers") {
    val pattern = "a{1}+b{1,}+c{1,2}+"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(parsedTree.children(0) match {
      case Quantifier(Character('a', _), 1, 1, _, PossessiveQuantifier, true) => true
      case _                                                                  => false
    })

    assert(parsedTree.children(1) match {
      case Quantifier(Character('b', _), 1, Quantifier.Infinity, _, PossessiveQuantifier, false) => true
      case _                                                                                     => false
    })

    assert(parsedTree.children(2) match {
      case Quantifier(Character('c', _), 1, 2, _, PossessiveQuantifier, false) => true
      case _                                                                   => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Unparsable: long quantifier with min > max") {
    val pattern = "a{2,1}"
    parseErrorTest(pattern)
  }

  test("Parse capturing group") {
    val pattern = "(hello)(world)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    parsedTree.children foreach (child =>
      assert(
        child match {
          case Group(_: Concat, true, _) => true
          case _                         => false
        },
        clue = parsedTree.children
      )
    )

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse nested capturing group") {
    val pattern = "(hello(world))"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assert(clue(parsedTree) match {
      case Group(Concat(nodes, _), true, _) =>
        assert(clue(nodes.last) match {
          case Group(_: Concat, true, _) => true
          case _                         => false
        })
        true
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse named capturing group") {
    val pattern = "(?<groupName1>hello)(?<GroupName2>world)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(clue(parsedTree.children.head) match {
      case NamedGroup(_: Concat, name, _) => name == "groupName1"
      case _                              => false
    })
    assert(clue(parsedTree.children.last) match {
      case NamedGroup(_: Concat, name, _) => name == "GroupName2"
      case _                              => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse nested named capturing group") {
    val pattern = "(?<groupName1>hello(?<GroupName2>world))"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assert(clue(parsedTree) match {
      case NamedGroup(Concat(nodes, _), "groupName1", _) =>
        assert(clue(nodes.last) match {
          case NamedGroup(_: Concat, "GroupName2", _) => true
          case _                                      => false
        })
        true
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse non-capturing group") {
    val pattern = "(?:hello)(?:world)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    parsedTree.children foreach (child =>
      assert(
        child match {
          case Group(_: Concat, false, _) => true
          case _                          => false
        },
        clue = parsedTree.children
      )
    )

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse nested non-capturing group") {
    val pattern = "(?:hello(?:world))"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assert(clue(parsedTree) match {
      case Group(Concat(nodes, _), false, _) =>
        assert(clue(nodes.last) match {
          case Group(_: Concat, false, _) => true
          case _                          => false
        })
        true
      case _ => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse positive lookahead") {
    val pattern = "(?=hello)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assert(clue(parsedTree) match {
      case Lookaround(_: Concat, true, true, _) => true
      case _                                    => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse negative lookahead") {
    val pattern = "(?!hello)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assert(clue(parsedTree) match {
      case Lookaround(_: Concat, false, true, _) => true
      case _                                     => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse positive lookbehind") {
    val pattern = "(?<=hello)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail

    assert(clue(parsedTree) match {
      case Lookaround(_: Concat, true, false, _) => true
      case _                                     => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse negative lookbehind") {
    val pattern = "(?<!hello)"
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Lookaround]

    assert(parsedTree.expr.isInstanceOf[Concat])
    assert(!clue(parsedTree).isPositive)
    assert(!clue(parsedTree).isLookahead)

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse named reference") {
    val pattern = """\k<name1>"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[NameReference]

    assertEquals(parsedTree.name, "name1")

    treeBuildTest(parsedTree, pattern)
  }

  test("Parse character quote") {
    val pattern = """stuff\$hit"""
    val parsedTree = Parser(pattern, parserFlavor).getOrFail.to[Concat]

    assert(clue(parsedTree.children(5)) match {
      case QuoteChar('$', _) => true
      case _                 => false
    })

    treeBuildTest(parsedTree, pattern)
  }

  test("Parser failure at start") {
    val pattern = "("
    parseErrorTest(pattern)
  }

  test("Parser failure mid-regex") {
    val pattern = "abc(def"
    parseErrorTest(pattern)
  }

  implicit class RegexTreeCastExtension(tree: RegexTree) {
    def to[T <: RegexTree](implicit ct: ClassTag[T], loc: Location): T = tree match {
      case t: T => t
      case _ =>
        fail(s"Expected '${ct.runtimeClass.getSimpleName}', but got '${tree.getClass.getSimpleName}'", clues(tree))
    }
  }
}
