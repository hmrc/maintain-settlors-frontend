import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / scalaVersion := "2.13.18"
ThisBuild / majorVersion := 0

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(CodeCoverageSettings())
  .settings(
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:cat=unused-imports&src=html/.*:s",
      "-Wconf:cat=unused-imports&src=routes/.*:s"
    ),
    inConfig(Test)(testSettings),
    name := "maintain-settlors-frontend",
    RoutesKeys.routesImport += "models._",
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._"
    ),
    PlayKeys.playDefaultPort := 9795,
    libraryDependencies ++= AppDependencies()
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.itDependencies)

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  javaOptions ++= Seq("-Dconfig.resource=test.application.conf")
)

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
