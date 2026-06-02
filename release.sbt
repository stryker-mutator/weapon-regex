import scala.sys.process.Process

commands ++= List(
  Command.command("WeaponRegeXPublishSigned")(
    "fullLinkJS" :: "+publishSigned" :: "writePackageJson" :: "publishNpmLatest" :: _
  )
)

lazy val publishNpmLatest = taskKey[Unit]("Publish to npm")
publishNpmLatest := {
  val command = Seq("npm", "publish")
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
