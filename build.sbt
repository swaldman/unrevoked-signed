organization := "com.mchange"

name := "unrevoked-signed"

version := "0.0.1-SNAPSHOT"

ethcfgScalaStubsPackage := "com.mchange.sc.v1.unrevokedsigned.contract"

// dependency crap

val nexus = "https://oss.sonatype.org/"
val nexusSnapshots = nexus + "content/repositories/snapshots";
val nexusReleases = nexus + "service/local/staging/deploy/maven2";

resolvers += ("releases" at nexusReleases)

resolvers += ("snapshots" at nexusSnapshots)
