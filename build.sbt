// Skip publish root
publish / skip := true

val Scala212 = "2.12.15"
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
    libraryDependencies += "com.lihaoyi" %%% "fastparse" % "2.3.3",
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test,
    testFrameworks += new TestFramework("munit.Framework"),
    // Fatal warnings only in CI
    scalacOptions --= (if (sys.env.exists { case (k, v) => k == "CI" && v == "true" }) Nil
                       else Seq("-Xfatal-warnings"))
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
      scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
      scalacOptions += scalaJSSourceUri.value
    )
  )

/** Map sourceURI to github location, taken from
  * https://github.com/typelevel/cats/blob/7ce35f50ced2ceb5747ec643333e38f0af866c1e/build.sbt#L186-L195
  */
lazy val scalaJSSourceUri = Def.task {
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
    mdocOut := file(".")
  )
  .jvmPlatform(scalaVersions = List(Scala213))
  .enablePlugins(MdocPlugin)
