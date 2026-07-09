# Changelog

## [2.0.4](https://github.com/stryker-mutator/weapon-regex/compare/v2.0.3...v2.0.4) (2026-07-09)


### Performance Improvements

* **parser:** parse expressions once instead of multiple times per nesting level ([#682](https://github.com/stryker-mutator/weapon-regex/issues/682)) ([d4171fa](https://github.com/stryker-mutator/weapon-regex/commit/d4171fa11a523e9e7c2b1d389685339b79b95b4b))
* **parser:** reorder parsing to try most-common cases first ([#685](https://github.com/stryker-mutator/weapon-regex/issues/685)) ([c9d8560](https://github.com/stryker-mutator/weapon-regex/commit/c9d85609addb0167166c566aa88d02a2dda6c103))
* **parser:** reuse already-parsed locations ([#684](https://github.com/stryker-mutator/weapon-regex/issues/684)) ([906b9fb](https://github.com/stryker-mutator/weapon-regex/commit/906b9fb0d9f0c1d5fe075671f22733379c35a873))

## [2.0.3](https://github.com/stryker-mutator/weapon-regex/compare/v2.0.2...v2.0.3) (2026-07-02)


### Bug Fixes

* add 'main' field to package.json ([#677](https://github.com/stryker-mutator/weapon-regex/issues/677)) ([61fc900](https://github.com/stryker-mutator/weapon-regex/commit/61fc900cde7a0683ce2c85e8c9eeb4a1d4de5cae))

## [2.0.2](https://github.com/stryker-mutator/weapon-regex/compare/v2.0.1...v2.0.2) (2026-06-28)


### Bug Fixes

* revert Scala to LTS (3.3.8) ([#672](https://github.com/stryker-mutator/weapon-regex/issues/672)) ([e72f695](https://github.com/stryker-mutator/weapon-regex/commit/e72f6950785201680c3e58741143e6c0e1cfc761))

## [2.0.1](https://github.com/stryker-mutator/weapon-regex/compare/v2.0.0...v2.0.1) (2026-06-25)


### Bug Fixes

* use js.Object for exported JS objects ([#669](https://github.com/stryker-mutator/weapon-regex/issues/669)) ([870d339](https://github.com/stryker-mutator/weapon-regex/commit/870d3394725ecb0297387927c872f71c2e9de3a6))

## [2.0.0](https://github.com/stryker-mutator/weapon-regex/compare/v1.4.1...v2.0.0) (2026-06-05)


### ⚠ BREAKING CHANGES

* use `mutationtesting.{Location, Position}` instead of `weaponregex.model.{Location, Position}` ([#653](https://github.com/stryker-mutator/weapon-regex/issues/653))
* use cats NonEmpty data structures for mutators and mutation levels ([#652](https://github.com/stryker-mutator/weapon-regex/issues/652))

### Features

* improve `Position`/`Location` checks and tests ([#650](https://github.com/stryker-mutator/weapon-regex/issues/650)) ([5cd7bea](https://github.com/stryker-mutator/weapon-regex/commit/5cd7bea646117bab228d3ca505ed2fe3e366d64d))
* use `mutationtesting.{Location, Position}` instead of `weaponregex.model.{Location, Position}` ([#653](https://github.com/stryker-mutator/weapon-regex/issues/653)) ([f0fe938](https://github.com/stryker-mutator/weapon-regex/commit/f0fe93845f868e88fc841ebb9a9bf19e8ec610e7))
* use cats NonEmpty data structures for mutators and mutation levels ([#652](https://github.com/stryker-mutator/weapon-regex/issues/652)) ([af7e6c5](https://github.com/stryker-mutator/weapon-regex/commit/af7e6c5e82185d9c27a168cd3a597e2714cc28ee))


### Bug Fixes

* add parser context for improved error messages ([#648](https://github.com/stryker-mutator/weapon-regex/issues/648)) ([881a7e3](https://github.com/stryker-mutator/weapon-regex/commit/881a7e3f90b1a8bc1f9119fe2b19f400f7d02361))

## [1.4.1](https://github.com/stryker-mutator/weapon-regex/compare/v1.4.0...v1.4.1) (2026-06-02)


### Miscellaneous Chores

* release 1.4.1 ([fd3f5f1](https://github.com/stryker-mutator/weapon-regex/commit/fd3f5f12126d3b54aec17d538ac58e3f8cedcadb))

## [1.4.0](https://github.com/stryker-mutator/weapon-regex/compare/v1.4.0...v1.4.0) (2026-06-02)

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
