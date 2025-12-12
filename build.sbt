import org.scalajs.linker.interface.{ESFeatures, ESVersion}
import org.typelevel.scalacoptions.{ScalaVersion, ScalacOption, ScalacOptions}
import org.typelevel.sbt.tpolecat.DevMode
import com.typesafe.tools.mima.core.{MissingMethodProblem, MissingTypesProblem, Problem, ProblemFilters}

// Skip publish root
publish / skip := true
disablePlugins(MimaPlugin)

val Scala212 = "2.12.21"
val Scala213 = "2.13.18"
val Scala3 = "3.3.7"

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
    libraryDependencies += "com.lihaoyi" %%% "fastparse" % "3.1.1",
    libraryDependencies += "org.scalameta" %%% "munit" % "1.2.1" % Test,
    tpolecatScalacOptions ++= Set(
      ScalacOptions.source3,
      ScalacOptions.release("8"),
      ScalacOptions.other("-Wconf:cat=scala3-migration:s", _.isBetween(ScalaVersion.V2_12_2, ScalaVersion.V3_0_0))
    ),
    tpolecatExcludeOptions ++= Set(ScalacOptions.warnNonUnitStatement, ScalacOptions.warnUnusedNoWarn),
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
      tpolecatScalacOptions += ScalacOptions.other(scalaJSSourceUri.value)
    )
  )

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

lazy val writePackageJson = taskKey[Unit]("Write package.json")
writePackageJson := IO.write(file("package.json"), generatePackageJson.value)

lazy val generatePackageJson = taskKey[String]("Generate package.json")
generatePackageJson := s"""{
                          |  "name": "${name.value}",
                          |  "type": "module",
                          |  "version": "${version.value}",
                          |  "description": "${description.value}",
                          |  "exports": {
                          |    ".": {
                          |      "types": "./index.d.ts",
                          |      "import": "./${(WeaponRegeX.js(Scala3) / Compile / fullLinkJSOutput).value
                           .relativeTo(file("."))
                           .get
                           .toString}/main.js"
                          |    }
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
                          |  "license": "${licenses.value.head._1}"
                          |}
                          |""".stripMargin
