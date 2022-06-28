import org.scalajs.linker.interface.{ESFeatures, ESVersion}

// Skip publish root
publish / skip := true

val Scala212 = "2.12.16"
val Scala213 = "2.13.8"

inThisBuild(
  List(
    organization := "io.stryker-mutator",
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
    )
  )
)

lazy val WeaponRegeX = projectMatrix
  .in(file("core"))
  .settings(
    name := "weapon-regex",
    libraryDependencies += "com.lihaoyi" %%% "fastparse" % "2.3.3",
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test,
    // Fatal warnings only in CI
    scalacOptions --= (if (sys.env.exists { case (k, v) => k == "CI" && v == "true" }) Nil
                       else Seq("-Xfatal-warnings")),
    scalacOptions += "-Xsource:3"
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
      scalacOptions += scalaJSSourceUri.value,
      genDev := writePackageJson(packageJsonDev.value),
      packageJsonDev := genPackage("fastopt").value,
      genProd := writePackageJson(packageJsonProd.value),
      packageJsonProd := genPackage("opt").value
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

lazy val genDev = taskKey[Unit]("generate package.json for dev")
lazy val genProd = taskKey[Unit]("generate package.json for release")
lazy val packageJsonDev = settingKey[String]("package.json for dev")
lazy val packageJsonProd = settingKey[String]("package.json for release")

def writePackageJson(pkg: String) = IO.write(file("package.json"), pkg)

def genPackage(opt: String) = Def.setting {
  s"""{
     |  "name": "${name.value}",
     |  "type": "module",
     |  "version": "${version.value}",
     |  "description": "${description.value}",
     |  "main": "${(target.value / s"${name.value}-${opt}" / "main.js").relativeTo(file(".")).get}",
     |  "repository": {
     |    "type": "git",
     |    "url": "${homepage.value.get}"
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
     |""".stripMargin,
}
