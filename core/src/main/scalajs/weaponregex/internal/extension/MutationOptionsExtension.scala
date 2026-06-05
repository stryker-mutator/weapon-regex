package weaponregex.internal.extension

import cats.data.{NonEmptyList, NonEmptySet}
import cats.syntax.all.*
import weaponregex.model.MutationOptions
import weaponregex.model.mutation.TokenMutator
import weaponregex.mutator.BuiltinMutators
import weaponregex.parser.{ParserFlavor, ParserFlavorJS}

private[weaponregex] object MutationOptionsExtension {

  /** The extension that converts a given [[weaponregex.model.MutationOptions]]
    */
  implicit class MutationOptionsConverter(val mutationOptions: MutationOptions) extends AnyVal {

    /** Convert to a Scala tuple of (mutators, mutationLevels, flavor)
      * @return
      *   A tuple of (mutators, mutationLevels, flavor)
      */
    def toScala: (NonEmptyList[TokenMutator], Option[NonEmptySet[Int]], ParserFlavor) = {
      val mutators: NonEmptyList[TokenMutator] =
        if (mutationOptions.hasOwnProperty("mutators") && mutationOptions.mutators != null)
          mutationOptions.mutators.toList.map(_.tokenMutator).toNel.getOrElse(BuiltinMutators.all)
        else BuiltinMutators.all

      val mutationLevels: Option[NonEmptySet[Int]] =
        if (mutationOptions.hasOwnProperty("mutationLevels") && mutationOptions.mutationLevels != null)
          mutationOptions.mutationLevels.toList.toNel.map(_.toNes)
        else None

      val flavor: ParserFlavor =
        if (mutationOptions.hasOwnProperty("flavor") && mutationOptions.flavor != null)
          mutationOptions.flavor
        else ParserFlavorJS

      (mutators, mutationLevels, flavor)
    }
  }
}
