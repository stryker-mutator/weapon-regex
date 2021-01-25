import scala.sys.process.Process

commands ++= List(
  Command.command("WeaponRegeXPublishSigned")(
    "fullLinkJS" :: "+publishSigned" :: "writePackageJson" :: "publishNpmLatest" :: _
  )
)

lazy val publishNpmNext = taskKey[Unit]("Publish to npm with next tag")
publishNpmNext := runNpmPublish("next")

lazy val publishNpmLatest = taskKey[Unit]("Publish to npm with latest tag")
publishNpmLatest := runNpmPublish("latest")

def runNpmPublish(tag: String): Unit = {
  val command = Seq("npm", "publish", "--tag", tag)
  val os = sys.props("os.name").toLowerCase
  val panderToWindows = os match {
    case n if n contains "windows" => Seq("cmd", "/C") ++ command
    case _                         => command
  }
  Process(panderToWindows).! match {
    case 0        =>
    case exitCode => throw new Exception(s"Exit code $exitCode")
  }
}

lazy val writePackageJson = taskKey[Unit]("Write package.json")
writePackageJson := IO.write(file("package.json"), generatePackageJson.value)

lazy val generatePackageJson = taskKey[String]("Generate package.json")
generatePackageJson := s"""{
                          |  "name": "${name.value}",
                          |  "version": "${version.value}",
                          |  "description": "${description.value}",
                          |  "main": "core/target/js-2.13/weapon-regex-opt/main.js",
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
                          |  "license": "Apache-2.0"
                          |}
                          |""".stripMargin
