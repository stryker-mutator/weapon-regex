/// <reference types="mocha" />
/// <reference types="node" />
// @ts-check
import * as wrx from '../../core/target/js-3/weapon-regex-fastopt/main.js';
import assert from 'assert';

// @ts-ignore
/** @type {import('../../')} */ const {
  mutate,
  mutators,
  ParserFlavorJS,
  ParserFlavorJVM,
} = wrx;

describe('Weapon regeX', () => {
  describe('#mutate()', () => {
    it('Can mutate without options', () => {
      const mutants = mutate('^a');
      assert.strictEqual(mutants.length, 2);
    });

    it('Can mutate with only mutators as option', () => {
      const mutants = mutate('^a', undefined, {
        mutators: Array.from(mutators.values()),
      });
      assert.strictEqual(mutants.length, 2);
    });

    it('Can mutate with only levels as option', () => {
      const mutants = mutate('^a', undefined, { mutationLevels: [1] });
      assert.strictEqual(mutants.length, 1);
    });

    it('Can mutate with both levels and mutators as options', () => {
      const mutants = mutate('^a', undefined, {
        mutators: Array.from(mutators.values()),
        mutationLevels: [1],
      });
      assert.strictEqual(mutants.length, 1);
    });

    it('Can mutate with JS regex flavor', () => {
      const mutants = mutate('\\x{20}', undefined, {
        flavor: ParserFlavorJS,
      });
      assert.strictEqual(mutants.length, 4);
    });

    it('Can mutate with JVM regex flavor', () => {
      const mutants = mutate('\\x{20}', undefined, {
        flavor: ParserFlavorJVM,
      });
      assert.strictEqual(mutants.length, 0);
    });

    it('Can mutate with flags', () => {
      const mutants = mutate('\\u{20}', 'u', {});
      assert.strictEqual(mutants.length, 0);
    });

    it('Can mutate with `undefined` as flags', () => {
      const mutants = mutate('\\u{20}', undefined, {});
      assert.strictEqual(mutants.length, 4);
    });

    it('Can mutate with `null` as flags', () => {
      const mutants = mutate('\\u{20}', null, {});
      assert.strictEqual(mutants.length, 4);
    });

    it('Can mutate with flags but no options param', () => {
      const mutants = mutate('\\u{20}', 'u');
      assert.strictEqual(mutants.length, 0);
    });

    it('Returns an empty array if there are no mutants', () => {
      const mutants = mutate('a');
      assert.deepStrictEqual(mutants, []);
    });

    it('Catch an exception if the RegEx is invalid', () => {
      assert.throws(
        () => mutate('*(a|$]'),
        (/** @type {Error} */ e) => e.message.startsWith('[Error] Parser: ')
      );
    });
  });

  describe('Mutant', () => {
    it('Contains the replacement pattern', () => {
      const mutants = mutate('^a', undefined, { mutationLevels: [1] });

      assert.strictEqual(mutants.length, 1);
      assert.strictEqual(mutants[0].pattern, 'a');
    });

    it('Contains the mutator name', () => {
      const mutants = mutate('^a', undefined, { mutationLevels: [1] });

      assert.strictEqual(mutants.length, 1);
      assert.strictEqual(
        mutants[0].name,
        'Beginning of line character `^` removal'
      );
    });

    it('Contains the location of the mutation', () => {
      const mutants = mutate('^a', undefined, { mutationLevels: [1] });

      assert.strictEqual(mutants.length, 1);
      assert.strictEqual(mutants[0].location.start.line, 0);
      assert.strictEqual(mutants[0].location.start.column, 0);
      assert.strictEqual(mutants[0].location.end.line, 0);
      assert.strictEqual(mutants[0].location.end.line, 0);
    });

    it('Contains the level of the mutator', () => {
      const mutants = mutate('^a', undefined, { mutationLevels: [1] });

      assert.strictEqual(mutants.length, 1);
      assert.deepStrictEqual(mutants[0].levels, [1, 2, 3]);
    });

    it('Contains the mutator description', () => {
      const mutants = mutate('^a', undefined, { mutationLevels: [1] });

      assert.strictEqual(mutants.length, 1);
      assert.strictEqual(
        mutants[0].description,
        mutants[0].location.show + ' Remove the beginning of line character `^`'
      );
    });

    it('Contains the mutator replacement', () => {
      const mutants = mutate('^a$', undefined, { mutationLevels: [1] });

      assert.strictEqual(mutants.length, 2);
      assert.strictEqual(mutants[0].replacement, '');
      assert.strictEqual(mutants[1].replacement, '');
    });
  });

  describe('mutators', () => {
    it('Is a map from String to mutator object', () => {
      Array.from(mutators.keys()).forEach((key) => {
        assert.strictEqual(typeof key, 'string');
      });
      Array.from(mutators.values()).forEach((value) => {
        assert.strictEqual(typeof value, 'object');
        assert.strictEqual(typeof value.name, 'string');
        assert(Array.isArray(value.levels));
      });
    });
  });
});
