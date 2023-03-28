import org.scalajs.linker.interface.{ESFeatures, ESVersion}
import _root_.io.github.davidgregory084.{DevMode, ScalacOption}

// Skip publish root
publish / skip := true

val Scala212 = "2.12.17"
val Scala213 = "2.13.10"

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
    libraryDependencies += "com.lihaoyi" %%% "fastparse" % "2.3.3",
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test,
    tpolecatScalacOptions += ScalacOptions.source3,
    tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement
  )
  .jvmPlatform(
    scalaVersions = List(Scala213, Scala212),
    settings = Seq(
      // Add JVM-specific settings here
      libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided"
    )
  )
  .jsPlatform(
    scalaVersions = List(Scala213, Scala212),
    settings = Seq(
      // Add JS-specific settings here
      scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.ESModule)
        .withESFeatures(ESFeatures.Defaults.withESVersion(ESVersion.ES2020))),
      scalacOptions += scalaJSSourceUri.value
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
    mdocOut := file("."),
    tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement
  )
  .jvmPlatform(scalaVersions = List(Scala213))
  .enablePlugins(MdocPlugin)
