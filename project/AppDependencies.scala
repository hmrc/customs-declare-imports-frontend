import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.22.0",
    "uk.gov.hmrc" %% "play-ui" % "7.22.0",
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % "3.8.0",
    "uk.gov.hmrc" %% "auth-client" % "2.9.0-play-25",
    "uk.gov.hmrc" %% "http-caching-client" % "7.1.0",
    "uk.gov.hmrc" %% "play-reactivemongo" % "6.2.0",
    "uk.gov.hmrc" %% "wco-dec" % "0.17.0"
  )

  def test(scope: String = "test") = Seq(
    "org.scalatest"          %% "scalatest" % "3.0.4" % scope,
    "org.pegdown"             % "pegdown" % "1.6.0" % scope,
    "org.jsoup"               % "jsoup" % "1.10.2" % scope,
    "com.typesafe.play"      %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
    "org.mockito"             % "mockito-core" % "2.13.0" % scope,
    "org.scalacheck"         %% "scalacheck" % "1.14.0" % scope
  )

}
