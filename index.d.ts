export interface Location {
  start: Position;
  end: Position;
}
export interface Position {
  line: number;
  column: number;
}

export const ParserFlavorJS: object;
export const ParserFlavorJVM: object;
export type ParserFlavor = typeof ParserFlavorJS | typeof ParserFlavorJVM;

export interface TokenMutator {
  name: string;
  levels: number[];
  description: string;
}

export interface MutationOptions {
  mutators?: TokenMutator[];
  mutationLevels?: number[];
  flavor?: ParserFlavor;
}

export interface Mutant {
  /** The replacement pattern
   */
  pattern: String;

  /** Name of the mutation
   */
  name: String;

  /** [[weaponregex.model.Location]] in the original string where the mutation occurred
   */
  location: Location;

  /** The mutation levels of the mutator
   */
  levels: number[];

  /** Description on the mutation
   */
  description: String;
}

/** Mutate using the given mutators at some specific mutation levels
 * @param pattern
 *   Input regex string
 * @param options
 *   JavaScript object for Mutation options
 *   {{{
 * {
 *   mutators: [Mutators to be used for mutation],
 *   mutationLevels: [Target mutation levels. If this is `null`, the `mutators` will not be filtered],
 * }
 *   }}}
 * @return
 *   A JavaScript Array of [[weaponregex.model.mutation.Mutant]] if can be parsed, or throw an exception otherwise
 */
export function mutate(pattern: string, options?: MutationOptions): Mutant[];

/** JS Map that maps from a token mutator class names to the associating token mutator
 */
export const mutators: Map<string, TokenMutator>;

export const BuiltinMutators: {
  /** JS Array of all built-in token mutators
   */
  all: TokenMutator[];

  /** JS Map that maps from a token mutator class names to the associating token mutator
   */
  byName: Map<string, TokenMutator>;

  /** JS Map that maps from mutation level number to token mutators in that level
   */
  byLevel: Map<number, TokenMutator[]>;

  /** Get all the token mutators in the given mutation level
   * @param mutationLevel
   *   Mutation level number
   * @return
   *   Array of all the tokens mutators in that level, if any
   */
  atLevel(mutationLevel: number): TokenMutator[];

  /** Get all the token mutators in the given mutation levels
   * @param mutationLevels
   *   Mutation level numbers
   * @return
   *   Array of all the tokens mutators in that levels, if any
   */
  atLevels(mutationLevels: number[]): TokenMutator[];
};
