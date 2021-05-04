// Skip publish root
skip in publish := true

val Scala212 = "2.12.13"
val Scala213 = "2.13.5"

inThisBuild(
  List(
    organization := "io.stryker-mutator",
    homepage := Some(url("https://github.com/stryker-mutator/weapon-regex")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        id = "nhat",
        name = "Nhat",
        email = "",
        url = url("http://github.com/Nhaajt")
      ),
      Developer(
        id = "jan",
        name = "Jan",
        email = "",
        url = url("http://github.com/JSmits-utwente")
      ),
      Developer(
        id = "wijtse",
        name = "Wijtse",
        email = "",
        url = url("http://github.com/wijtserekker")
      )
    )
  )
)

lazy val WeaponRegeX = projectMatrix
  .in(file("core"))
  .settings(
    name := "weapon-regex",
    libraryDependencies += "com.lihaoyi" %%% "fastparse" % "2.3.2",
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.25" % Test,
    testFrameworks += new TestFramework("munit.Framework")
  )
  .jvmPlatform(
    scalaVersions = List(Scala213, Scala212),
    settings = Seq(
      // Add JVM-specific settings here
      libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided",
      jacocoReportSettings := JacocoReportSettings().withThresholds(JacocoThresholds(line = 80))
    )
  )
  .jsPlatform(
    scalaVersions = List(Scala213, Scala212),
    settings = Seq(
      // Add JS-specific settings here
      scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
    )
  )

lazy val docs = projectMatrix
  .in(file("wr-docs"))
  .dependsOn(WeaponRegeX)
  .settings(
    mdocOut := file("."),
    mdocVariables := Map(
      "VERSION" -> previousStableVersion.value.get
    )
  )
  .jvmPlatform(scalaVersions = List(Scala213))
  .enablePlugins(MdocPlugin)
