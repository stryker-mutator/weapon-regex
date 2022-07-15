package weaponregex.extension

object EitherExtension {
  implicit class LeftStringEitherTest[+B](either: Either[String, B]) extends munit.FunSuite {

    /** Get the value of `Right` if this is a `Right`. Otherwise, fail the test with the message from `Left`.
      * @return
      *   The value of `Right`
      */
    def getOrFail: B = either.fold(fail(_), identity)
  }
}
