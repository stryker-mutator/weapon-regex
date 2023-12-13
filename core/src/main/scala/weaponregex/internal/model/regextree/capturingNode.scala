package weaponregex.internal.model.regextree

import weaponregex.model.Location

/** (Non-)capturing group node
  *
  * @param expr
  *   The regex inside the group
  * @param isCapturing
  *   `true` if group is capturing, `false` otherwise
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Group(
    expr: RegexTree,
    isCapturing: Boolean,
    override val location: Location
) extends Node(Seq(expr), location, if (isCapturing) "(" else "(?:", ")")

/** Named capturing group node
  * @param expr
  *   The regex inside the group
  * @param name
  *   The name of the group
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class NamedGroup(expr: RegexTree, name: String, override val location: Location)
    extends Node(Seq(expr), location, s"(?<$name>", ")")

/** Non-capturing group with flags
  * @param flagToggle
  *   The [[weaponregex.internal.model.regextree.FlagToggle]] object associated with the group
  * @param expr
  *   The regex inside the group
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class FlagNCGroup(
    flagToggle: FlagToggle,
    expr: RegexTree,
    override val location: Location
) extends Node(Seq(flagToggle, expr), location, "(?", ")", ":")

/** Flag toggle group node
  * @param flagToggle
  *   The [[weaponregex.internal.model.regextree.FlagToggle]] object associated with the group
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class FlagToggleGroup(flagToggle: FlagToggle, override val location: Location)
    extends Node(Seq(flagToggle), location, "(?", ")")

/** Flag toggle node that describes which flags are toggled on and/or off
  * @param onFlags
  *   The flags that are toggled on
  * @param hasDash
  *   `true` if there is a dash character `-` between the `onFlags` and `offFlags`, `false` otherwise
  * @param offFlags
  *   The flags that are toggled off
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class FlagToggle(onFlags: Flags, hasDash: Boolean, offFlags: Flags, override val location: Location)
    extends Node(Seq(onFlags, offFlags), location)

/** A sequence of flags for use in [[weaponregex.internal.model.regextree.FlagToggle]]
  * @param flags
  *   The sequence of flag [[weaponregex.internal.model.regextree.Character]] s
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Flags(flags: Seq[Character], override val location: Location) extends Node(flags, location)

/** The umbrella node for positive/negative lookahead/lookbehind
  * @param expr
  *   The regex inside the lookaround
  * @param isPositive
  *   `true` if the lookaround is positive, `false` otherwise
  * @param isLookahead
  *   `true` if this is a lookahead, `false` if this is a lookbehind
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class Lookaround(expr: RegexTree, isPositive: Boolean, isLookahead: Boolean, override val location: Location)
    extends Node(
      Seq(expr),
      location,
      "(?"
        + (if (isLookahead) "" else "<")
        + (if (isPositive) "=" else "!"),
      ")"
    )

/** Atomic (independent, non-capturing) group node
  * @param expr
  *   The regex inside the group
  * @param location
  *   The [[weaponregex.model.Location]] of the node in the regex string
  */
case class AtomicGroup(expr: RegexTree, override val location: Location) extends Node(Seq(expr), location, "(?>", ")")
