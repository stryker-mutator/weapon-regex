package weaponregex.extension

import weaponregex.model.regextree._

object RegexTreeExtension {
  implicit class RegexTreeStringBuilder(tree: RegexTree) {

    /** Build the tree into a String
      */
    lazy val build: String = tree match {
      case leaf: Leaf[_]  => leaf.prefix + leaf.value + leaf.postfix
      case ft: FlagToggle => ft.onFlags.build + (if (ft.hasDash) "-" else "") + ft.offFlags.build
      case _              => buildWhile(_ => true)
    }

    /** Build the tree into a String with a child replaced by a string.
      * @param child Child to be replaced
      * @param childString Replacement String
      * @return A String representation of the tree
      */
    def buildWith(child: RegexTree, childString: String): String = tree match {
      case _: Leaf[_] => build
      case _ =>
        tree.children
          .map(c => if (c eq child) childString else c.build)
          .mkString(tree.prefix, tree.sep, tree.postfix)
    }

    /** Build the tree into a String while a predicate holds for a given child.
      * @param pred Predicate on a child
      * @return A String representation of the tree
      */
    def buildWhile(pred: RegexTree => Boolean): String = tree match {
      case _: Leaf[_] => build
      case _ =>
        tree.children
          .filter(pred)
          .map(_.build)
          .mkString(tree.prefix, tree.sep, tree.postfix)
    }
  }
}
