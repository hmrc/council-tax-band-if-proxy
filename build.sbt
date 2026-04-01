import uk.gov.hmrc.DefaultBuildSettings.{itSettings, targetJvm}

val appName = "council-tax-band-if-proxy"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / targetJvm := "jvm-21"
ThisBuild / scalacOptions ++= Seq("-feature", "-Wconf:msg=Flag .* set repeatedly:s")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(
    PlayKeys.playDefaultPort := 8882,
    libraryDependencies ++= AppDependencies.appDependencies,
    maintainer := "voa.service.optimisation@digital.hmrc.gov.uk",
    scalacOptions += "-Wconf:src=routes/.*:s",
    javaOptions += "-XX:+EnableDynamicAgentLoading"
  )

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    libraryDependencies ++= AppDependencies.itDependencies
  )
  .settings(itSettings())
