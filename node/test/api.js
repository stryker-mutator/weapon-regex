const wrx = require('../../core/target/js-2.13/weapon-regex-fastopt/main')
const mocha = require('assert');

describe('Weapon regeX', function() {
  describe('#mutate()', function() {
    
    it('Can mutate without options', function() {
      let mutants = wrx.mutate('^a');
      mocha.strictEqual(mutants.length, 2);
    });
    
    it('Can mutate with only mutators as option', function() {
      let mutants = wrx.mutate('^a', {mutators: Array.from(wrx.mutators.values())});
      mocha.strictEqual(mutants.length, 2);
    });
    
    it('Can mutate with only levels as option', function() {
      let mutants = wrx.mutate('^a', {mutationLevels: [1]});
      mocha.strictEqual(mutants.length, 1);
    });
    
    it('Can mutate with both levels and mutators as options', function() {
      let mutants = wrx.mutate('^a', {mutators: Array.from(wrx.mutators.values()), mutationLevels: [1]});
      mocha.strictEqual(mutants.length, 1);
    });
    
    it('Returns an empty array if there are no mutants', function() {
      let mutants = wrx.mutate('a');
      mocha.deepStrictEqual(mutants, []);
    });
    
    it('Catch an exception if the RegEx is invalid', function() {
      let failed = false;
      try {
        wrx.mutate('*(a|$]');
      } catch(e) {
        failed = true;
        mocha.strictEqual(e.message.startsWith("[Error] Parser:"), true)
      }
      mocha.strictEqual(failed, true);
    });
  });
  
  describe('Mutant', function() {
    
    it('Contains the replacement pattern', function() {
      let mutants = wrx.mutate('^a', {mutationLevels: [1]});

      mocha.strictEqual(mutants.length, 1);
      mocha.strictEqual(mutants[0].pattern, 'a');
    });
    
    it('Contains the mutator name', function() {
      let mutants = wrx.mutate('^a', {mutationLevels: [1]});

      mocha.strictEqual(mutants.length, 1);
      mocha.strictEqual(mutants[0].name, 'Beginning of line character `^` removal');
    });
    
    it('Contains the location of the mutation', function() {
      let mutants = wrx.mutate('^a', {mutationLevels: [1]});

      mocha.strictEqual(mutants.length, 1);
      mocha.strictEqual(mutants[0].location.start.line, 0);
      mocha.strictEqual(mutants[0].location.start.column, 0);
      mocha.strictEqual(mutants[0].location.end.line, 0);
      mocha.strictEqual(mutants[0].location.end.line, 0);
    });
    
    it('Contains the level of the mutator', function() {
      let mutants = wrx.mutate('^a', {mutationLevels: [1]});

      mocha.strictEqual(mutants.length, 1);
      mocha.deepStrictEqual(mutants[0].levels, [1, 2, 3]);
    });
    
    it('Contains the mutator description', function() {
      let mutants = wrx.mutate('^a', {mutationLevels: [1]});

      mocha.strictEqual(mutants.length, 1);
      mocha.strictEqual(mutants[0].description, 'Remove beginning of line character `^`');
    });
  });

  describe('mutators', function() {
    it('Is a map from String to mutator object', function() {
    
      Array.from(wrx.mutators.keys()).forEach(key => {
        mocha.strictEqual(typeof key, 'string')
      });
      Array.from(wrx.mutators.values()).forEach(value => {
        mocha.strictEqual(typeof value, 'object')
        mocha.strictEqual(typeof value.name, 'string')
        mocha.strictEqual(Array.isArray(value.levels), true)
      });

    });   
  });
});

