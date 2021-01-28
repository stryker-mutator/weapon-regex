# Weapon regeX
[![Mutation testing badge](https://img.shields.io/endpoint?style=flat&url=https%3A%2F%2Fbadge-api.stryker-mutator.io%2Fgithub.com%2Fstryker-mutator%2Fweapon-regex%2Fmain)](https://dashboard.stryker-mutator.io/reports/github.com/stryker-mutator/weapon-regex/main)
[![Build Status](https://github.com/stryker-mutator/weapon-regex/workflows/Scala%20CI/badge.svg)](https://github.com/stryker-mutator/weapon-regex/actions?query=workflow%3AScala%20CI+branch%3Amain)
[![GitHub Pages](https://img.shields.io/static/v1?label=GitHub%20Pages&message=Try%20it!&color=blue&logo=github)](https://stryker-mutator.github.io/weapon-regex/)

<img src="images/WeaponRegeX_logo.svg" width="50%" alt="Weapon regeX Logo">

With Weapon regeX you can mutate regular expressions which can be used in mutation testing. The 
generated regular expressions cover edge cases and typos. Weapon regeX is available for both
Javascript and Scala. The Javascript version of the library is generated from Scala using [ScalaJS](https://www.scala-js.org/).

The current supported versions for Scala are: `2.12.12` and `2.13.3`.

# Getting started

## Scala
Add Weapon regeX to your ```build.sbt```.
```scala
"io.stryker-mutator" %% "weapon-regex" % "0.3.0"
```

Mutate!

```scala
import weaponregex.WeaponRegeX
import scala.util.{Success, Failure}

WeaponRegeX.mutate("^abc(d+|[xyz])$") match {
  case Success(mutants) => mutants map (_.pattern) map println
  case Failure(e)       => print(e.getMessage)
}
```

## Javascript

Install Weapon regeX with `npm`.

```bash
npm install weapon-regex
```

Mutate!

```javascript
const wrx = require('weapon-regex');

let mutants = wrx.mutate("^abc(d+|[xyz])$");

mutants.forEach(mutant => {
  console.log(mutant.pattern);
});
```

[![Try it!](https://img.shields.io/static/v1?label=RunKit&message=Try%20it!&color=F55FA6&logo=runkit)](https://npm.runkit.com/weapon-regex)

# API
## Scala

The ```mutate``` function has the following signature:

```scala
//import scala.util.{Try, Success, Failure}

def mutate(
      pattern: String,
      mutators: Seq[TokenMutator] = BuiltinMutators.all,
      mutationLevels: Seq[Int] = null
  ): Try[Seq[Mutant]]
```
With the ```mutators``` argument you can give a select list of mutators that should be used in
the mutation process. If omitted, all builtin mutators will be used. This list will be filtered
depending on the ```mutationLevels``` argument.

A list of ```mutationLevels``` can also be passed to the function. The mutators will be filtered
based on the levels in the list. If omitted, no filtering takes place.

This function will return a `Success` of of a sequence of `Mutant` if can be parsed, a `Failure` otherwise.

## Javascript

The ```mutate``` function can be called with an options object to control which mutators should be
used in the mutation process:

```Javascript
const wrx = require('weapon-regex');

let mutants = wrx.mutate("^abc(d+|[xyz])$",{
  mutators: Array.from(wrx.mutators.values()),
  mutationLevels: [1, 2, 3]
});
```

Both options can be omitted, and have the same functionality as the options described in the Scala
API section. You can get a map of mutators from the ```mutators``` attribute of the library. It is
a map from string (mutator name) to a mutator object.

This function will return a JavaScript Array of `Mutant` if can be parsed, or throw an exception otherwise.

# Supported mutators
All the supported mutators and at which mutation level they appear are shown in the table below.

Name | 1 | 2 | 3
--- | --- | --- | ---
[BOLRemoval](#BOLRemoval) | ✅ | ✅ | ✅
[EOLRemoval](#EOLRemoval) | ✅ | ✅ | ✅
[BOL2BOI](#BOL2BOI) | | ✅ | ✅
[EOL2EOI](#EOL2EOI) | | ✅ | ✅
[CharClassNegation](#CharClassNegation) | ✅
[CharClassChildRemoval](#CharClassChildRemoval) | | ✅ | ✅
[CharClassAnyChar](#CharClassAnyChar) | | ✅ | ✅
[CharClassRangeModification](#CharClassRangeModification) | | | ✅
[PredefCharClassNegation](#PredefCharClassNegation) | ✅
[PredefCharClassNullification](#PredefCharClassNullification) | | ✅ | ✅
[PredefCharClassAnyChar](#PredefCharClassAnyChar) | | ✅ | ✅
[QuantifierRemoval](#QuantifierRemoval) | ✅
[QuantifierNChange](#QuantifierNChange) | | ✅ | ✅
[QuantifierNOrMoreModification](#QuantifierNOrMoreModification) | | ✅ | ✅
[QuantifierNOrMoreChange](#QuantifierNOrMoreChange) | | ✅ | ✅
[QuantifierNMModification](#QuantifierNMModification) | | ✅ | ✅
[QuantifierShortModification](#QuantifierShortModification) | | ✅ | ✅
[QuantifierShortChange](#QuantifierShortChange) | | ✅ | ✅
[QuantifierReluctantAddition](#QuantifierReluctantAddition) | | | ✅
[GroupToNCGroup](#GroupToNCGroup) | | ✅ | ✅

## Boundary Mutators

### BOLRemoval

It removes the beginning of line character "```^```"

Original | Mutated
--- | ---
```^abc```|```abc```

[Back to table 🔝](#Supported-mutators)

### EOLRemoval

It removes the end of line character "```$```"

Original | Mutated
--- | ---
```abc$```|```abc```

[Back to table 🔝](#Supported-mutators)

### BOL2BOI

It changes the beginning of line character "```^```" to a beginning of input character "```\A```"

Original | Mutated
--- | ---
```^abc```|```\Aabc```

[Back to table 🔝](#Supported-mutators)

### EOL2EOI

It changes the end of line character "```^```" to a end of input character "```\z```"

Original | Mutated
--- | ---
```abc$```|```abc\z```

[Back to table 🔝](#Supported-mutators)

## Character class mutators

### CharClassNegation

It flips the sign of a character class.

Original | Mutated
--- | ---
```[abc]```|```[^abc]```
```[^abc]```|```[abc]```

[Back to table 🔝](#Supported-mutators)

### CharClassChildRemoval

It removes a child of a character class.

Original | Mutated
--- | ---
```[abc]```|```[bc]```
```[abc]```|```[ac]```
```[abc]```|```[ab]```

[Back to table 🔝](#Supported-mutators)

### CharClassAnyChar

It changes a character class to a character class which matches any character.

Original | Mutated
--- | ---
```[abc]```|```[\w\W]```

[Back to table 🔝](#Supported-mutators)

### CharClassRangeModification

It changes the high and low of a range by one in both directions if possible.

Original | Mutated
--- | ---
```[b-y]```|```[a-y]```
```[b-y]```|```[c-y]```
```[b-y]```|```[b-z]```
```[b-y]```|```[b-x]```

[Back to table 🔝](#Supported-mutators)

## Predefined character class mutators

### PredefCharClassNegation

It flips the sign of a predefined character class. All the predefined character classes are shown in the table below.

Original | Mutated
--- | ---
```\d```|```\D```
```\D```|```\d```
```\s```|```\S```
```\S```|```\s```
```\w```|```\W```
```\W```|```\w```

[Back to table 🔝](#Supported-mutators)

### PredefCharClassNullification

It removes the backslash from a predefined character class such as "```\w```".

Original | Mutated
--- | ---
```\d```|```d```
```\D```|```D```
```\s```|```s```
```\S```|```S```
```\w```|```w```
```\W```|```W```

[Back to table 🔝](#Supported-mutators)

### PredefCharClassAnyChar

It changes a predefined character class to a character class containing the predefined one and its
negation.

Original | Mutated
--- | ---
```\d```|```[\d\D]```
```\D```|```[\D\d]```
```\s```|```[\s\S]```
```\S```|```[\S\s]```
```\w```|```[\w\W]```
```\W```|```[\W\w]```

[Back to table 🔝](#Supported-mutators)

## Quantifier mutators

### QuantifierRemoval

It removes a quantifier. This is done for all possible quantifiers, even ranges, and the reluctant
and possessive variants.

Original | Mutated
--- | ---
```abc?```|```abc```
```abc*```|```abc```
```abc+```|```abc```
```abc{1,3}```|```abc```
```abc??```|```abc```
```abc*?```|```abc```
```abc+?```|```abc```
```abc{1,3}?```|```abc```
```abc?+```|```abc```
```abc*+```|```abc```
```abc++```|```abc```
```abc{1,3}+```|```abc```

[Back to table 🔝](#Supported-mutators)

### QuantifierNChange

It changes the fixed amount quantifier to a couple range variants.

Original | Mutated
--- | ---
```abc{9}```|```abc{0,9}```
```abc{9}```|```abc{9,}```

[Back to table 🔝](#Supported-mutators)

### QuantifierNOrMoreModification

It changes the n to infinity range quantifier to a couple variants where the low of the range is
incremented an decremented by one.

Original | Mutated
--- | ---
```abc{9,}```|```abc{8,}```
```abc{9,}```|```abc{10,}```

[Back to table 🔝](#Supported-mutators)

### QuantifierNOrMoreChange

It turns an N or more range quantifier into a fixed number quantifier.

Original | Mutated
--- | ---
```abc{9,}```|```abc{9}```

[Back to table 🔝](#Supported-mutators)

### QuantifierNMModification

It alters the N to M range quantifier by decrementing or incrementing the high and low of the
range by one.

Original | Mutated
--- | ---
```abc{3,9}```|```abc{2,9}```
```abc{3,9}```|```abc{4,9}```
```abc{3,9}```|```abc{3,8}```
```abc{3,9}```|```abc{3,10}```

[Back to table 🔝](#Supported-mutators)

### QuantifierShortModification

It treats the shorthand quantifiers (`?`, `*`, `+`) as their corresponding range quantifier
variant (`{0,1}`, `{0,}`, `{1,}`), and applies the same mutations as mentioned in the mutators
above.

Original | Mutated
--- | ---
```abc?```|```abc{1,1}```
```abc?```|```abc{0,0}```
```abc?```|```abc{0,2}```
```abc*```|```abc{1,}```
```abc+```|```abc{0,}```
```abc+```|```abc{2,}```

[Back to table 🔝](#Supported-mutators)

### QuantifierShortChange

It changes the shorthand quantifiers `*` and `+` to their fixed range quantifier variant.

Original | Mutated
--- | ---
```abc*```|```abc{0}```
```abc+```|```abc{1}```

[Back to table 🔝](#Supported-mutators)

### QuantifierReluctantAddition

It changes greedy quantifiers to reluctant quantifiers.

Original | Mutated
--- | ---
```abc?```|```abc??```
```abc*```|```abc*?```
```abc+```|```abc+?```
```abc{9}```|```abc{9}?```
```abc{9,}```|```abc{9,}?```
```abc{9,13}```|```abc{9,13}?```

[Back to table 🔝](#Supported-mutators)

## Group mutators

### GroupToNCGroup

It changes a normal group to a non-capturing group.

Original | Mutated
--- | ---
```(abc)```|```(?:abc)```

[Back to table 🔝](#Supported-mutators)


_Copyright 2021 Stryker mutator team_