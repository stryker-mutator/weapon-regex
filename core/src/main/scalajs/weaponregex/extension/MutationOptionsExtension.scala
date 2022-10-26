package weaponregex.extension

import weaponregex.model.MutationOptions
import weaponregex.model.mutation.TokenMutator
import weaponregex.mutator.BuiltinMutators
import weaponregex.parser.{ParserFlavor, ParserFlavorJS}

object MutationOptionsExtension {

  /** The extension that converts a given [[MutationOptions]]
    */
  implicit class MutationOptionsConverter(mutationOptions: MutationOptions) {

    /** Convert to a Scala tuple of (mutators, mutationLevels, flavor)
      * @return
      *   A tuple of (mutators, mutationLevels, flavor)
      */
    def toScala: (Seq[TokenMutator], Seq[Int], ParserFlavor) = {
      val mutators: Seq[TokenMutator] =
        if (mutationOptions.hasOwnProperty("mutators") && mutationOptions.mutators != null)
          mutationOptions.mutators.toSeq map (_.tokenMutator)
        else BuiltinMutators.all

      val mutationLevels: Seq[Int] =
        if (mutationOptions.hasOwnProperty("mutationLevels") && mutationOptions.mutationLevels != null)
          mutationOptions.mutationLevels.toSeq
        else null

      val flavor: ParserFlavor =
        if (mutationOptions.hasOwnProperty("flavor") && mutationOptions.flavor != null)
          mutationOptions.flavor
        else ParserFlavorJS

      (mutators, mutationLevels, flavor)
    }
  }
}
