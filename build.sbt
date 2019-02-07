import TestPhases.oneForkedJvmPerTest
import org.scalastyle.sbt.ScalastylePlugin._
import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "customs-declare-imports-frontend"

scalastyleConfig := baseDirectory.value / "project" / "scalastyle-config.xml"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    majorVersion := 0
  )
  .settings(
    publishingSettings: _*
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false,
    addTestReportOption(IntegrationTest, "int-test-reports")
  )
  .settings(
    resolvers ++= Seq(Resolver.jcenterRepo, Resolver.bintrayRepo("hmrc", "releases"))
  )
  .settings(scoverageSettings)
  .settings(scalafmtOnCompile := true)

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := List(
    "<empty>",
    "Reverse.*",
    "domain\\..*",
    "forms\\..*",
    "views\\..*",
    ".*(BuildInfo|Routes|Options|FeatureSwitchController|TestingUtilitiesController).*"
  ).mkString(";"),
  coverageMinimum := 70,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  parallelExecution in Test := false
)
