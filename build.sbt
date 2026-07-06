import org.scalajs.linker.interface.{ESFeatures, ESVersion}
import org.typelevel.scalacoptions.{ScalaVersion, ScalacOption, ScalacOptions}
import org.typelevel.sbt.tpolecat.DevMode
import com.typesafe.tools.mima.core.{
  DirectMissingMethodProblem,
  MissingMethodProblem,
  MissingTypesProblem,
  Problem,
  ProblemFilters
}

commands ++= List(
  Command.command("WeaponRegeXPublishSigned")(
    "fullLinkJS" :: "+publishSigned" :: "WeaponRegeXJS/npmPublish" :: _
  )
)

lazy val root = rootProject
  .disablePlugins(MimaPlugin)
  .settings(
    publish / skip := true
  )
  .autoAggregate

val Scala212 = "2.12.21"
val Scala213 = "2.13.18"
val Scala3 = "3.3.8"

inThisBuild(
  List(
    organization := "io.stryker-mutator",
    description := "Weapon regeX mutates regular expressions for use in mutation testing.",
    homepage := Some(url("https://github.com/stryker-mutator/weapon-regex")),
    licenses := List(License.Apache2),
    developers := List(
      Developer(
        id = "nhat",
        name = "Nhat",
        email = "",
        url = url("https://github.com/Nhaajt")
      ),
      Developer(
        id = "jan",
        name = "Jan",
        email = "",
        url = url("https://github.com/JSmits-utwente")
      ),
      Developer(
        id = "wijtse",
        name = "Wijtse",
        email = "",
        url = url("https://github.com/wijtserekker")
      )
    ),
    versionScheme := Some(VersionScheme.SemVerSpec),
    // Fatal warnings only in CI
    tpolecatCiModeEnvVar := "CI",
    tpolecatReleaseModeEnvVar := "CI_RELEASE",
    tpolecatDefaultOptionsMode := DevMode
  )
)

lazy val WeaponRegeX = projectMatrix
  .in(file("core"))
  .settings(
    name := "weapon-regex",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "1.1.0",
      "io.stryker-mutator" %% "mutation-testing-metrics" % "3.8.4",
      "org.scalameta" %% "munit" % "1.3.3" % Test
    ),
    tpolecatScalacOptions ++= Set(
      ScalacOptions.source("3", version => version.isBetween(ScalaVersion.V2_12_0, ScalaVersion.V2_13_0)),
      ScalacOptions.source("3-cross", version => version.isBetween(ScalaVersion.V2_13_0, ScalaVersion.V3_0_0)),
      ScalacOptions.release("17")
    ),
    Test / tpolecatExcludeOptions ++= Set(ScalacOptions.warnNonUnitStatement, ScalacOptions.warnUnusedNoWarn),
    // To introduce a breaking version, comment out these lines and add `.disablePlugins(MimaPlugin)` after the .settings()
    mimaPreviousArtifacts := previousStableVersion.value
      .map(previousVersion => organization.value %% name.value % previousVersion)
      .toSet,
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[Problem]("weaponregex.internal.*"),
      // Adding fields to Mutant is not considered a breaking change
      ProblemFilters.exclude[MissingMethodProblem]("weaponregex.model.mutation.Mutant.*"),
      ProblemFilters.exclude[MissingTypesProblem]("weaponregex.model.mutation.Mutant$")
    )
  )
  .jvmPlatform(
    scalaVersions = List(Scala3, Scala213, Scala212),
    settings = Seq(
      // Add JVM-specific settings here
      libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided"
    )
  )
  .jsPlatform(
    scalaVersions = List(Scala3, Scala213, Scala212),
    settings = Seq(
      // Add JS-specific settings here
      scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.ESModule)
        .withESFeatures(ESFeatures.Defaults.withESVersion(ESVersion.ES2020))),
      tpolecatScalacOptions += ScalacOptions.other(scalaJSSourceUri.value),
      // Emit the linker output under scala-<binary> (e.g. scala-3) instead of scala-<full> (e.g. scala-3.8.4)
      Compile / fastLinkJS / scalaJSLinkerOutputDirectory := jsLinkerBinaryVersionDir.value / s"${moduleName.value}-fastopt",
      Compile / fullLinkJS / scalaJSLinkerOutputDirectory := jsLinkerBinaryVersionDir.value / s"${moduleName.value}-opt",

      generatePackageJson :=
        s"""{
           |  "name": "${name.value}",
           |  "type": "module",
           |  "version": "${version.value}",
           |  "description": "${description.value}",
           |  "main": "./main.js",
           |  "types": "./index.d.ts",
           |  "exports": {
           |    ".": {
           |      "types": "./index.d.ts",
           |      "import": "./main.js"
           |    },
           |    "./package.json": "./package.json"
           |  },
           |  "repository": {
           |    "type": "git",
           |    "url": "git+${homepage.value.get}.git"
           |  },
           |  "keywords": [
           |    "regex",
           |    "regexp",
           |    "regular expression",
           |    "mutate",
           |    "mutation",
           |    "mutator"
           |  ],
           |  "license": "${licenses.value.head.spdxId}"
           |}
           |""".stripMargin,
      npmPackage := Def.uncached {
        val outDir = (Compile / fullLinkJSOutput).value
        val packageJsonContent = generatePackageJson.value
        val root = (LocalRootProject / baseDirectory).value

        IO.write(outDir / "package.json", packageJsonContent)
        IO.copyFile(root / "index.d.ts", outDir / "index.d.ts")
        IO.copyFile(root / "README.md", outDir / "README.md")
        streams.value.log.info(s"Created NPM project in ${outDir}")

        outDir
      },
      npmPublish := Def.uncached {
        // Assemble the package, then publish from the linker output directory.
        val outDir = npmPackage.value
        val command = Seq("npm", "publish")
        val os = sys.props("os.name").toLowerCase
        val panderToWindows =
          if (os.contains("windows")) Seq("cmd", "/C") ++ command
          else command

        scala.sys.process.Process(panderToWindows, outDir).! match {
          case 0        =>
          case exitCode => throw new Exception(s"Exit code $exitCode")
        }
      }
    )
  )

lazy val jsLinkerBinaryVersionDir = Def.setting {
  val ct = crossTarget.value
  ct.getParentFile.getParentFile / s"scala-${scalaBinaryVersion.value}" / ct.getName
}

/** Map sourceURI to github location, taken from
  * https://github.com/typelevel/cats/blob/7ce35f50ced2ceb5747ec643333e38f0af866c1e/build.sbt#L186-L195
  */
lazy val scalaJSSourceUri = Def.setting {
  val tagOrHash =
    if (isSnapshot.value) git.gitHeadCommit.value.get
    else "v" + version.value
  val a = (LocalRootProject / baseDirectory).value.toURI.toString
  val g = "https://raw.githubusercontent.com/stryker-mutator/weapon-regex/" + tagOrHash
  val opt = if (scalaVersion.value.startsWith("3.")) "-scalajs-mapSourceURI" else "-P:scalajs:mapSourceURI"
  s"$opt:$a->$g/"
}

lazy val docs = projectMatrix
  .in(file("wr-docs"))
  .dependsOn(WeaponRegeX)
  .settings(
    publish / skip := true,
    mdocOut := file("."),
    tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement
  )
  .jvmPlatform(scalaVersions = List(Scala3))
  .enablePlugins(MdocPlugin)
  .disablePlugins(MimaPlugin)

lazy val generatePackageJson = taskKey[String]("Generate package.json")
lazy val npmPackage = taskKey[File]("Assemble the npm package in the Scala.js linker output dir")
lazy val npmPublish = taskKey[Unit]("Publish to npm")
