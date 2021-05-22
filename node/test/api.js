const wrx = require('../../core/target/js-2.13/weapon-regex-fastopt/main');
const assert = require('assert');

describe('Weapon regeX', () => {
  describe('#mutate()', () => {
    it('Can mutate without options', () => {
      const mutants = wrx.mutate('^a');
      assert.strictEqual(mutants.length, 2);
    });

    it('Can mutate with only mutators as option', () => {
      const mutants = wrx.mutate('^a', {
        mutators: Array.from(wrx.mutators.values()),
      });
      assert.strictEqual(mutants.length, 2);
    });

    it('Can mutate with only levels as option', () => {
      const mutants = wrx.mutate('^a', { mutationLevels: [1] });
      assert.strictEqual(mutants.length, 1);
    });

    it('Can mutate with both levels and mutators as options', () => {
      const mutants = wrx.mutate('^a', {
        mutators: Array.from(wrx.mutators.values()),
        mutationLevels: [1],
      });
      assert.strictEqual(mutants.length, 1);
    });

    it('Returns an empty array if there are no mutants', () => {
      const mutants = wrx.mutate('a');
      assert.deepStrictEqual(mutants, []);
    });

    it('Catch an exception if the RegEx is invalid', () => {
      assert.throws(
        () => wrx.mutate('*(a|$]'),
        (e) => e.message.startsWith('[Error] Parser: ')
      );
    });
  });

  describe('Mutant', () => {
    it('Contains the replacement pattern', () => {
      const mutants = wrx.mutate('^a', { mutationLevels: [1] });

      assert.strictEqual(mutants.length, 1);
      assert.strictEqual(mutants[0].pattern, 'a');
    });

    it('Contains the mutator name', () => {
      const mutants = wrx.mutate('^a', { mutationLevels: [1] });

      assert.strictEqual(mutants.length, 1);
      assert.strictEqual(
        mutants[0].name,
        'Beginning of line character `^` removal'
      );
    });

    it('Contains the location of the mutation', () => {
      const mutants = wrx.mutate('^a', { mutationLevels: [1] });

      assert.strictEqual(mutants.length, 1);
      assert.strictEqual(mutants[0].location.start.line, 0);
      assert.strictEqual(mutants[0].location.start.column, 0);
      assert.strictEqual(mutants[0].location.end.line, 0);
      assert.strictEqual(mutants[0].location.end.line, 0);
    });

    it('Contains the level of the mutator', () => {
      const mutants = wrx.mutate('^a', { mutationLevels: [1] });

      assert.strictEqual(mutants.length, 1);
      assert.deepStrictEqual(mutants[0].levels, [1, 2, 3]);
    });

    it('Contains the mutator description', () => {
      const mutants = wrx.mutate('^a', { mutationLevels: [1] });

      assert.strictEqual(mutants.length, 1);
      assert.strictEqual(
        mutants[0].description,
        'Remove beginning of line character `^`'
      );
    });
  });

  describe('mutators', () => {
    it('Is a map from String to mutator object', () => {
      Array.from(wrx.mutators.keys()).forEach((key) => {
        assert.strictEqual(typeof key, 'string');
      });
      Array.from(wrx.mutators.values()).forEach((value) => {
        assert.strictEqual(typeof value, 'object');
        assert.strictEqual(typeof value.name, 'string');
        assert(Array.isArray(value.levels));
      });
    });
  });
});
