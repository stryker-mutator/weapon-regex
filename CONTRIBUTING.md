# Contribute to Weapon regeX
So you want to contribute to this project? Well you can! All you have to do is follow the guidelines in this file.

## Team members
* Bui Hong Thien Nhat
* Wijtse Rekker
* Jan Smits

## Running Weapon regeX locally
In order to run and test the project, the installation of two dependencies is required:

* [SBT](https://www.scala-sbt.org/1.x/docs/Setup.html)
* [npm](https://www.npmjs.com/get-npm)

Follow their respective installation instructions to get started. Additionally, if you are using vscode we recommend installing the [Scala (Metals)](https://marketplace.visualstudio.com/items?itemName=scalameta.metals) extension. If you are developing in the IntelliJ IDE, we recommend the [Scala](https://plugins.jetbrains.com/plugin/1347-scala) plugin.

### Scala Tests

To run the Scala tests, execute the following command from the project root:
```
sbt test
```
or (if you want to include code coverage)
```
sbt jacoco
```

### Code formatting
We recommend to use the project's default settings for scalafmt, which should format your code on file save. If for some reason you want to check your code for formatting errors with a command you can run:
```
sbt scalafmtCheck
```
And to automatically format your code you can run:
```
sbt scalafmt
```

### JS integration tests
To run the JS integration tests you first need to generate a new JS version of Weapon regeX with the following command:
```
sbt fastLinkJS
```
Then, change directory to the `node` directory and run the test with these commands:
```
cd node
npm test
```

## Adding features
Before you start working on a new feature or a bugfix, please create an issue first or let us know via a different medium. After that, follow the steps below:

1. Create a fork of the repository on your own github account.
2. While writing your code, conform to the formatting rules of scalafmt as discussed above.
3. Create or alter unit tests for your new code.
4. Make sure that all tests pass by following the testing steps above.
5. When creating commits, please conform to [the angular commit message style](https://docs.google.com/document/d/1rk04jEuGfk9kYzfqCuOlPTSJw3hEDZJTBN5E5f1SALo/edit).
   Namely in the form `<type>(<scope>): <subject>\n\n[body]`
   * Type: feat, fix, docs, style, refactor, test, chore.
   * Scope can the the file or group of files (not a strict right or wrong)
   * Subject and body: present tense (~changed~*change*, ~added~*add*) and include motivation and contrasts with previous behavior

## Reporting bugs
You can also contribute to the project by reporting bugs in the issue tracker! Do make sure if the bug is related to parsing that your input regex is correct by checking it with an external syntax checker, and that the same error occurs when testing it in the [demo page](https://stryker-mutator.io/weapon-regex). Please describe in detail:
* The input parameters to reproduce the erroneous behavior
* The error message, or unexpected behavior
* If applicable, the expected behavior
