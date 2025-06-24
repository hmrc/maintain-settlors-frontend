import sbt.*

object AppDependencies {

  val bootstrapVersion = "9.13.0"
  val mongoVersion = "2.6.0"

  private lazy val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"                     % mongoVersion,
    "uk.gov.hmrc"             %% "play-frontend-hmrc-play-30"             % "12.1.0",
    "uk.gov.hmrc"             %% "domain-play-30"                         % "11.0.0",
    "uk.gov.hmrc"             %% "play-conditional-form-mapping-play-30"  % "3.3.0",
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-30"             % bootstrapVersion
  )

  private lazy val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-test-play-30"   % bootstrapVersion,
    "uk.gov.hmrc.mongo"           %% "hmrc-mongo-test-play-30"  % mongoVersion,
    "org.scalatest"               %% "scalatest"                % "3.2.19",
    "org.scalatestplus"           %% "scalacheck-1-17"          % "3.2.18.0",
    "org.scalatestplus"           %% "mockito-5-10"             % "3.2.18.0",
    "org.jsoup"                   %  "jsoup"                    % "1.20.1",
    "wolfendale"                  %% "scalacheck-gen-regexp"    % "0.1.2",
    "com.vladsch.flexmark"        %  "flexmark-all"             % "0.64.8"
  ).map(_ % Test)

  val itDependencies: Seq[ModuleID] = Seq()

  def apply(): Seq[ModuleID] = compile ++ test
}
