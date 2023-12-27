export interface Location {
  start: Position;
  end: Position;

  get show(): string;
}

export interface Position {
  line: number;
  column: number;
}

// Classes with markers so the union type works properly
declare class ParserFlavorJSClass {
  private __marker: 'js';
}

declare class ParserFlavorJVMClass {
  private __marker: 'jvm';
}

export const ParserFlavorJS: ParserFlavorJSClass;
export const ParserFlavorJVM: ParserFlavorJVMClass;

export type ParserFlavor = typeof ParserFlavorJS | typeof ParserFlavorJVM;

export interface TokenMutator {
  name: string;
  levels: number[];
}

export interface MutationOptions {
  mutators?: TokenMutator[];
  mutationLevels?: number[];
  flavor?: ParserFlavor;
}

export interface Mutant {
  /** The replacement pattern
   */
  pattern: string;

  /** Name of the mutation
   */
  name: string;

  /** [[weaponregex.model.Location]] in the original string where the mutation occurred
   */
  location: Location;

  /** The mutation levels of the mutator
   */
  levels: number[];

  /** Description on the mutation
   */
  description: string;

  /** The part of the pattern that has been changed
   */
  replacement: string;
}

/** Mutate a regex pattern and flags with the given options.
 *
 * @param pattern
 *   Input regex string
 * @param flags
 *   Optional regex flags
 * @param options
 *   Optional JavaScript object for Mutation options
 *   {{{
 * {
 *   mutators: [Mutators to be used for mutation. If this is `null`, all built-in mutators will be used.],
 *   mutationLevels: [Target mutation levels. If this is `null`, the `mutators`, will not be filtered.],
 *   flavor: [Regex flavor. By the default, `ParerFlavorJS` will be used.]
 * }
 *   }}}
 * @return
 *   A JavaScript Array of [[weaponregex.model.mutation.Mutant]] if can be parsed, or throw an exception otherwise
 */
export function mutate(
  pattern: string,
  flags?: string | null,
  options?: MutationOptions | null
): Mutant[];

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
