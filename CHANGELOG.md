# Changelog

## [1.4.0](https://github.com/stryker-mutator/weapon-regex/compare/v1.4.0...v1.4.0) (2026-06-02)


### ⚠ BREAKING CHANGES

* **esm:** generated js is now pure ESM.

### Features

* `mutate` and `parse` returns a `Either` instead of `Try` ([#172](https://github.com/stryker-mutator/weapon-regex/issues/172)) ([89e8ef6](https://github.com/stryker-mutator/weapon-regex/commit/89e8ef67b060bfbc37e111d7b69fbcbbb802640f))
* add replacement field to Mutant ([#274](https://github.com/stryker-mutator/weapon-regex/issues/274)) ([2b2dfc8](https://github.com/stryker-mutator/weapon-regex/commit/2b2dfc8182f9d261f8a3b1c55a8b6bdbbfce2c72))
* add Scala 3 support ([#215](https://github.com/stryker-mutator/weapon-regex/issues/215)) ([aba6f20](https://github.com/stryker-mutator/weapon-regex/commit/aba6f200601085ea69082c78ed0b7aa010179a09))
* better mutant descriptions ([#268](https://github.com/stryker-mutator/weapon-regex/issues/268)) ([7a457cc](https://github.com/stryker-mutator/weapon-regex/commit/7a457ccadbb1f8db5c162f68a62c1f2561d41784))
* **esm:** publish to NPM with native ESM ([#168](https://github.com/stryker-mutator/weapon-regex/issues/168)) ([a57bb31](https://github.com/stryker-mutator/weapon-regex/commit/a57bb31449c1af928a0779555c86bc183c322e04))
* **format:** Reformat code ([26a3059](https://github.com/stryker-mutator/weapon-regex/commit/26a30596f47ef7799397fdf6dec2eeb8a6d25aa6))
* **format:** Reformat code ([a6b84d3](https://github.com/stryker-mutator/weapon-regex/commit/a6b84d35492a59a9121954269de0723bfd2d94ff))
* **logo:** Update new logo ([cc6ac69](https://github.com/stryker-mutator/weapon-regex/commit/cc6ac697f144f15df0f4d9405c73984339d6705a))
* **parser-js:** Initial support for the `v` flag for `unicodeSets` ([d324e09](https://github.com/stryker-mutator/weapon-regex/commit/d324e0922fe798151b94f2f0efdf8c7dfa9c67ee))
* **parser:** Change `UnicodeCharClass.propValue` to type `Option[String]` ([be7f1b7](https://github.com/stryker-mutator/weapon-regex/commit/be7f1b7dae72549d5f7b963e5301d787308ef1f5))
* **parser:** Support parsing Unicode Character Class with property and value `\p{property=value}` ([fdb4088](https://github.com/stryker-mutator/weapon-regex/commit/fdb4088e32adb2f4e6094f67f9b0158724d73dfd))
* **parser:** switch over to cats-parse ([#642](https://github.com/stryker-mutator/weapon-regex/issues/642)) ([431a22c](https://github.com/stryker-mutator/weapon-regex/commit/431a22c511c5ab776695874fe023addd6be0c485))
* **readme:** Update readme with new logo ([a759de9](https://github.com/stryker-mutator/weapon-regex/commit/a759de9d5926e79f7eff2f46ed3af686b7e50860))


### Bug Fixes

* add setup-node for release step publishing ([#644](https://github.com/stryker-mutator/weapon-regex/issues/644)) ([b6bfff8](https://github.com/stryker-mutator/weapon-regex/commit/b6bfff8787eda58456b1f3df8ef9ba3d5718a420))
* **docs:** Fix docs links ([#169](https://github.com/stryker-mutator/weapon-regex/issues/169)) ([925065e](https://github.com/stryker-mutator/weapon-regex/commit/925065e5413e493bb137b1f39840924b793dacf8))
* **format:** Reformat code ([b7e94a3](https://github.com/stryker-mutator/weapon-regex/commit/b7e94a38faccc9a705dd8399ee44572c9a33adae))
* **group-name:** Fix valid group name ([#300](https://github.com/stryker-mutator/weapon-regex/issues/300)) ([f9789b1](https://github.com/stryker-mutator/weapon-regex/commit/f9789b1cdf030fb221c7875e10de8a36dffb9955))
* **ParserJS:** fix reading flags when multiple flags are passed ([#176](https://github.com/stryker-mutator/weapon-regex/issues/176)) ([9abdbc4](https://github.com/stryker-mutator/weapon-regex/commit/9abdbc435b1e2f03cd46d7075f1b72fc652ee9da))
* **scaladoc:** Fix scaladoc link ([e6093f8](https://github.com/stryker-mutator/weapon-regex/commit/e6093f8f4d8394cf7e728671464002bbab032312))
* **scaladoc:** Fix scaladoc link ([464c3c3](https://github.com/stryker-mutator/weapon-regex/commit/464c3c3da8cac3a13f3d4fa303cc2b5e9be06acf))
* update release-please token ([#271](https://github.com/stryker-mutator/weapon-regex/issues/271)) ([3bc614a](https://github.com/stryker-mutator/weapon-regex/commit/3bc614a5efe76ea913de774419be65c6fc4a5ebb))
* use correct git info for stryker-mutator bot ([#290](https://github.com/stryker-mutator/weapon-regex/issues/290)) ([ccdc34c](https://github.com/stryker-mutator/weapon-regex/commit/ccdc34c49862822d17e599fcdeb3298b2c8121a2))


### Miscellaneous Chores

* release 1.3.3 ([8bf215a](https://github.com/stryker-mutator/weapon-regex/commit/8bf215ae9acdc9e5c043ae64a0ed4e71c13052ab))
* release 1.3.4 with OIDC publishing ([f73d6e3](https://github.com/stryker-mutator/weapon-regex/commit/f73d6e345325757b7f651a9c6c1bad5f91990f67))
* release 1.3.5 with OIDC publishing ([e54aa12](https://github.com/stryker-mutator/weapon-regex/commit/e54aa1226363f0b75e7c3aba83645f2d225d5eb1))
* release 1.3.6 with OIDC publishing ([f679cb8](https://github.com/stryker-mutator/weapon-regex/commit/f679cb8d7bad6bd3de4642ce61918fad8b757e65))
* release 1.4.0 ([f2b3f6a](https://github.com/stryker-mutator/weapon-regex/commit/f2b3f6a44e0442f2364f4502d60be1111d5be60b))


### Build System

* update scala.js to 1.16.0 ([#316](https://github.com/stryker-mutator/weapon-regex/issues/316)) ([6a30210](https://github.com/stryker-mutator/weapon-regex/commit/6a30210f9f1b78436670ced3982d62bebbb9bb51))

## [1.4.0](https://github.com/stryker-mutator/weapon-regex/compare/v1.3.6...v1.4.0) (2026-06-02)


### Features

* **parser:** switch over to cats-parse ([#642](https://github.com/stryker-mutator/weapon-regex/issues/642)) ([431a22c](https://github.com/stryker-mutator/weapon-regex/commit/431a22c511c5ab776695874fe023addd6be0c485))

## [1.3.6](https://github.com/stryker-mutator/weapon-regex/compare/v1.3.5...v1.3.6) (2025-09-23)


### Miscellaneous Chores

* release 1.3.6 with OIDC publishing ([f679cb8](https://github.com/stryker-mutator/weapon-regex/commit/f679cb8d7bad6bd3de4642ce61918fad8b757e65))

## [1.3.5](https://github.com/stryker-mutator/weapon-regex/compare/v1.3.4...v1.3.5) (2025-09-23)


### Miscellaneous Chores

* release 1.3.5 with OIDC publishing ([e54aa12](https://github.com/stryker-mutator/weapon-regex/commit/e54aa1226363f0b75e7c3aba83645f2d225d5eb1))

## [1.3.4](https://github.com/stryker-mutator/weapon-regex/compare/v1.3.3...v1.3.4) (2025-09-23)


### Miscellaneous Chores

* release 1.3.4 with OIDC publishing ([f73d6e3](https://github.com/stryker-mutator/weapon-regex/commit/f73d6e345325757b7f651a9c6c1bad5f91990f67))

## [1.3.3](https://github.com/stryker-mutator/weapon-regex/compare/v1.3.2...v1.3.3) (2025-08-04)


### Miscellaneous Chores

* release 1.3.3 ([8bf215a](https://github.com/stryker-mutator/weapon-regex/commit/8bf215ae9acdc9e5c043ae64a0ed4e71c13052ab))

## [1.3.2](https://github.com/stryker-mutator/weapon-regex/compare/v1.3.1...v1.3.2) (2024-03-19)


### Build System

* update scala.js to 1.16.0 ([#316](https://github.com/stryker-mutator/weapon-regex/issues/316)) ([6a30210](https://github.com/stryker-mutator/weapon-regex/commit/6a30210f9f1b78436670ced3982d62bebbb9bb51))

## [1.3.1](https://github.com/stryker-mutator/weapon-regex/compare/v1.3.0...v1.3.1) (2024-02-18)


### Bug Fixes

* **group-name:** Fix valid group name ([#300](https://github.com/stryker-mutator/weapon-regex/issues/300)) ([f9789b1](https://github.com/stryker-mutator/weapon-regex/commit/f9789b1cdf030fb221c7875e10de8a36dffb9955))
* use correct git info for stryker-mutator bot ([#290](https://github.com/stryker-mutator/weapon-regex/issues/290)) ([ccdc34c](https://github.com/stryker-mutator/weapon-regex/commit/ccdc34c49862822d17e599fcdeb3298b2c8121a2))

## [1.3.0](https://github.com/stryker-mutator/weapon-regex/compare/v1.2.1...v1.3.0) (2023-12-27)


### Features

* add replacement field to Mutant ([#274](https://github.com/stryker-mutator/weapon-regex/issues/274)) ([2b2dfc8](https://github.com/stryker-mutator/weapon-regex/commit/2b2dfc8182f9d261f8a3b1c55a8b6bdbbfce2c72))

## [1.2.1](https://github.com/stryker-mutator/weapon-regex/compare/v1.2.0...v1.2.1) (2023-12-13)


### Bug Fixes

* update release-please token ([#271](https://github.com/stryker-mutator/weapon-regex/issues/271)) ([3bc614a](https://github.com/stryker-mutator/weapon-regex/commit/3bc614a5efe76ea913de774419be65c6fc4a5ebb))

## [1.2.0](https://github.com/stryker-mutator/weapon-regex/compare/v1.1.1...v1.2.0) (2023-12-13)


### Features

* better mutant descriptions ([#268](https://github.com/stryker-mutator/weapon-regex/issues/268)) ([7a457cc](https://github.com/stryker-mutator/weapon-regex/commit/7a457ccadbb1f8db5c162f68a62c1f2561d41784))
* Support parsing Unicode Character Class with property and value `\p{property=value}` ([#249](https://github.com/stryker-mutator/weapon-regex/pull/249)) ([cc504365](https://github.com/stryker-mutator/weapon-regex/pull/249/commits/cc504365b46da3380564e6bd3aac5d53ed6b8130))

### Refactor

* **packages**: move internal code to internal package ([#267](https://github.com/stryker-mutator/weapon-regex/pull/267)) ([6ae8441](https://github.com/stryker-mutator/weapon-regex/commit/6ae8441d4e5e2815a1ef20e36542e19d0cb2d599))
