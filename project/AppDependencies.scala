import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val jacksonVersion = "2.9.6"

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.22.0",
    "uk.gov.hmrc" %% "play-ui" % "7.18.0",
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % "1.7.0",
    "uk.gov.hmrc" %% "auth-client" % "2.6.0",
    "uk.gov.hmrc" %% "http-caching-client" % "7.0.0",
    "uk.gov.hmrc" %% "play-reactivemongo" % "6.2.0",
    "uk.gov.hmrc" %% "wco-dec" % "0.4.0",
    "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
    "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % jacksonVersion,
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-properties" % jacksonVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
  )

  def test(scope: String = "test") = Seq(
    "org.scalatest" %% "scalatest" % "3.0.4" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.10.2" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
    "org.mockito" % "mockito-core" % "2.13.0" % scope,
    "com.github.tomakehurst" % "wiremock" % "2.6.0" % scope
  )

}
