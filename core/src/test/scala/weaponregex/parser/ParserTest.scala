package weaponregex.parser

import weaponregex.model.regextree._
import weaponregex.`extension`.RegexTreeExtension.RegexTreeStringBuilder

import scala.util.Failure

abstract class ParserTest extends munit.FunSuite {
  val parserFlavor: ParserFlavor

  def treeBuildTest(tree: RegexTree, pattern: String): Unit = assertEquals(tree.build, pattern)

  def parseErrorTest(pattern: String): Unit = {
    val parsedTree = Parser(pattern, parserFlavor)

    assert(clue(parsedTree) match {
      case Failure(exception: RuntimeException) => exception.getMessage.startsWith("[Error] Parser:")
      case _                                    => false
    })
  }
}
