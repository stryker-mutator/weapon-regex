package weaponregex.internal.extension

private[weaponregex] object StringExtension {

  implicit class StringStylingExtension(val string: String) extends AnyVal {

    /** Character-wise toggling the case of a String
      * @return
      *   Case-toggled String
      */
    final def toggleCase: String = string.map(_.toggleCase)
  }

  implicit class CharStylingExtension(val char: Char) extends AnyVal {

    /** Toggle the case of a character
      * @return
      *   Case-toggled character
      */
    final def toggleCase: Char = if (char.isUpper) char.toLower else char.toUpper
  }
}
