import scala.sys.process.Process

commands ++= List(
  Command.command("WeaponRegeXPublishSigned")(
    "fullLinkJS" :: "+publishSigned" :: "WeaponRegeXJS/genProd" :: "publishNpmLatest" :: _
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
