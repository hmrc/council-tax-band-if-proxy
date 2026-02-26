import uk.gov.hmrc.DefaultBuildSettings.{itSettings, targetJvm}

val appName = "council-tax-band-if-proxy"

ThisBuild / scalaVersion := "3.8.2"
ThisBuild / majorVersion := 0

val commonSettings = Seq(
  targetJvm := "jvm-21",
  scalacOptions += "-Wconf:msg=Flag .* set repeatedly:s"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(commonSettings)
  .settings(
    PlayKeys.playDefaultPort := 8882,
    libraryDependencies ++= AppDependencies.appDependencies,
    maintainer := "voa.service.optimisation@digital.hmrc.gov.uk",
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-feature",
    javaOptions += "-XX:+EnableDynamicAgentLoading"
  )

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(commonSettings)
  .settings(itSettings())
