import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.7.0"

  // Test dependencies
  private val voTestVersion    = "0.2.0"

  private val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion
  )

  private val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "uk.gov.hmrc" %% "vo-unit-test"           % voTestVersion    % Test
  )

  val appDependencies: Seq[ModuleID] = compile ++ test

  val itDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "vo-integration-test" % voTestVersion % Test
  )

}
