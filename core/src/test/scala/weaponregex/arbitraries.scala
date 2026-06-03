package weaponregex

import org.scalacheck.*
import weaponregex.model.*

/** Instances for Scalacheck property testing. Used in tests to generate random values
  */
object arbitraries {
  implicit def arbPosition: Arbitrary[Position] = Arbitrary {
    for {
      line <- Gen.posNum[Int]
      column <- Gen.posNum[Int]
    } yield Position(line, column)
  }

  implicit def arbLocation: Arbitrary[Location] = Arbitrary {
    for {
      startLine <- Gen.posNum[Int]
      startColumn <- Gen.posNum[Int]
      endLine <- Gen.choose(startLine, startLine + 10)
      endColumn <- if (endLine > startLine) Gen.posNum[Int] else Gen.choose(startColumn, startColumn + 10)
    } yield Location(Position(startLine, startColumn), Position(endLine, endColumn))
  }

  implicit def cogenPosition: Cogen[Position] = Cogen[(Int, Int)].contramap(p => (p.line, p.column))

  implicit def cogenLocation: Cogen[Location] = Cogen[(Position, Position)].contramap(l => (l.start, l.end))
}
