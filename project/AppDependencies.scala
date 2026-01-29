import sbt.*

object AppDependencies {

  val bootstrapVersion = "10.5.0"
  val mongoVersion     = "2.12.0"

  private lazy val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % mongoVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % "12.28.0",
    "uk.gov.hmrc"       %% "domain-play-30"                        % "11.0.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "3.4.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion
  )

  private lazy val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % mongoVersion,
    "org.scalatestplus" %% "scalacheck-1-18"         % "3.2.19.0",
    "wolfendale"        %% "scalacheck-gen-regexp"   % "0.1.2"
  ).map(_ % Test)

  val itDependencies: Seq[ModuleID] = Seq()

  def apply(): Seq[ModuleID] = compile ++ test
}
