// Skip publish root
publish / skip := true

val Scala212 = "2.12.13"
val Scala213 = "2.13.6"

inThisBuild(
  List(
    organization := "io.stryker-mutator",
    homepage := Some(url("https://github.com/stryker-mutator/weapon-regex")),
    licenses := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
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
    )
  )
)

lazy val WeaponRegeX = projectMatrix
  .in(file("core"))
  .settings(
    name := "weapon-regex",
    libraryDependencies += "com.lihaoyi" %%% "fastparse" % "2.3.2",
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.26" % Test,
    testFrameworks += new TestFramework("munit.Framework"),
    // Fatal warnings only in CI
    scalacOptions --= (if (sys.env.exists({ case (k, v) => k == "CI" && v == "true" })) Nil
                       else Seq("-Xfatal-warnings"))
  )
  .jvmPlatform(
    scalaVersions = List(Scala213, Scala212),
    settings = Seq(
      // Add JVM-specific settings here
      libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided"
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
      "VERSION" -> previousStableVersion.value.getOrElse(version.value)
    )
  )
  .jvmPlatform(scalaVersions = List(Scala213))
  .enablePlugins(MdocPlugin)
