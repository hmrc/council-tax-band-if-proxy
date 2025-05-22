import uk.gov.hmrc.DefaultBuildSettings.itSettings

val appName = "council-tax-band-if-proxy"

ThisBuild / scalaVersion := "3.7.0"
ThisBuild / majorVersion := 0

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(
    PlayKeys.playDefaultPort := 8882,
    libraryDependencies ++= AppDependencies.appDependencies,
    maintainer := "voa.service.optimisation@digital.hmrc.gov.uk",
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:msg=Flag .* set repeatedly:s",
    javaOptions += "-XX:+EnableDynamicAgentLoading"
  )

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())
  .settings(
    scalacOptions += "-Wconf:msg=Flag .* set repeatedly:s"
  )
