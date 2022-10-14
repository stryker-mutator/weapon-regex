package weaponregex.model

import weaponregex.model.mutation.TokenMutatorJS
import weaponregex.parser.{ParserFlavor, ParserFlavorJS}

import scala.scalajs.js

/**
  * A JavaScript object containing mutation options
  * @param mutators Mutators to be used for mutation. If this is `null`, all built-in mutators will be used.
  * @param mutationLevels Target mutation levels. If this is `null`, the `mutators` will not be filtered.
  * @param flavor Regex flavor. By the default, `ParerFlavorJS` will be used.
  */
class MutationOptions(
    val mutators: js.Array[TokenMutatorJS] = null,
    val mutationLevels: js.Array[Int] = null,
    val flavor: ParserFlavor = ParserFlavorJS
) extends js.Object
