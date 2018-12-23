val nexus = "https://oss.sonatype.org/"
val nexusSnapshots = nexus + "content/repositories/snapshots";
val nexusReleases = nexus + "service/local/staging/deploy/maven2";

ThisBuild / organization := "com.mchange"
ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / resolvers += ("releases" at nexusReleases)
ThisBuild / resolvers += ("snapshots" at nexusSnapshots)

lazy val root = (project in file(".")).settings (
  name := "unrevoked-signed",
  ethcfgScalaStubsPackage := "com.mchange.sc.v1.unrevokedsigned.contract"
)



