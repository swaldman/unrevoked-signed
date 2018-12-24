val Nexus = "https://oss.sonatype.org/"
val NexusSnapshots = Nexus + "content/repositories/snapshots";
val NexusReleases = Nexus + "service/local/staging/deploy/maven2";

ThisBuild / organization := "com.mchange"
ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / resolvers += ("releases" at NexusReleases)
ThisBuild / resolvers += ("snapshots" at NexusSnapshots)

lazy val root = (project in file(".")).settings (
  name                    := "unrevoked-signed",
  ethcfgScalaStubsPackage := "com.mchange.sc.v1.unrevokedsigned.contract",
  publishTo               := findPublishTo( version.value ),
  pomExtra                := createPomExtra( name.value )
)

lazy val plugin = (project in file("plugin")).aggregate(root).dependsOn( root ).settings (
  name                   := "unrevoked-signed-plugin",
  sbtPlugin              := true,
  publishTo              := findPublishTo( version.value ),
  pomExtra               := createPomExtra( name.value ),
  addSbtPlugin("com.mchange" % "sbt-ethereum" % "0.1.7-SNAPSHOT" changing())
)

def findPublishTo( version : String ) = {
  if (version.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at NexusSnapshots )
  else
    Some("releases"  at NexusReleases )
}

def createPomExtra( projectName : String ) = (
    <url>https://github.com/swaldman/{projectName}</url>
    <licenses>
      <license>
        <name>GNU Lesser General Public License, Version 2.1</name>
        <url>http://www.gnu.org/licenses/lgpl-2.1.html</url>
        <distribution>repo</distribution>
      </license>
      <license>
        <name>Eclipse Public License, Version 1.0</name>
        <url>http://www.eclipse.org/org/documents/epl-v10.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:swaldman/{projectName}.git</url>
      <connection>scm:git:git@github.com:swaldman/{projectName}</connection>
    </scm>
    <developers>
      <developer>
        <id>swaldman</id>
        <name>Steve Waldman</name>
        <email>swaldman@mchange.com</email>
      </developer>
    </developers>
)




