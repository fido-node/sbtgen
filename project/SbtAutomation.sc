import $ivy.`io.7mind.izumi.sbt:sbtgen_2.13:0.0.104`
import izumi.sbtgen._
import izumi.sbtgen.model._

case class Projection(name: String, aggregates: Seq[Aggregate])

object Main {
  def printHelpAndExit() = {
    System.out.println(""" Usage:
                         | ./sbtgen.sc <projection>
                         | where <projection> is one of:
                         |  all
                         |  util
                         |  fridge
                         |  cooktop
                         |""".stripMargin)
    System.exit(1)
  }

  def entrypoint(args: Seq[String]) = {

    if (args.length != 1 || args.headOption.exists(_.isEmpty)) {
      printHelpAndExit()
    }

    val projection = {
      args.headOption.map {
        case "all"     => Whisk.Projections.All
        case "util"    => Whisk.Projections.Util
        case "fridge"  => Whisk.Projections.Fridge
        case "cooktop" => Whisk.Projections.Cooktop
      }
    }
    if (projection.isEmpty) {
      printHelpAndExit()
    } else {
      Entrypoint.main(
        Sbt.rootProject(projection.get),
        Sbt.settings,
        Seq("-o", ".")
      )
    }
  }
}

object Whisk {
  object Projections {
    val All = Projection(
      name = "all",
      aggregates = Seq(
        Sbt.aggregate(
          Seq(
            Artifacts.Util.Art,
            Artifacts.Fridge.Art,
            Artifacts.Cooktop.Art
          )
        )
      )
    )
    val Util = Projection(
      name = "util",
      aggregates = Seq(
        Sbt.aggregate(
          Seq(
            Artifacts.Util.Art
          )
        )
      )
    )
    val Fridge = Projection(
      name = "fridge",
      aggregates = Seq(
        Sbt.aggregate(
          Seq(
            Artifacts.Util.Art,
            Artifacts.Fridge.Art
          )
        )
      )
    )
    val Cooktop = Projection(
      name = "cooktop",
      aggregates = Seq(
        Sbt.aggregate(
          Seq(
            Artifacts.Util.Art,
            Artifacts.Cooktop.Art
          )
        )
      )
    )
  }

  object Settings {

    val credentials =
      """Credentials(Path.userHome / ".m2" / ".credentials")""".raw

    val resolver =
      """"internal.repo.read" at "https://nexus.whisk-dev.com/repository/whisk-maven-group/"""".raw

    val circeIsDigusting =
      """
        |Seq(
        |        "io.circe" %% "circe-core" % VersionScheme.Always,
        |        "io.circe" %% "circe-parser" % VersionScheme.Always
        |      )
        |""".stripMargin.raw
  }
  object Libraries {
    def Util(libName: String): Library = Library(
      "com.whisk",
      s"util-${libName}",
      Version.VExpr("V.whiskSbtPlugin"),
      LibraryType.Auto
    )
  }
  object Projects {}
  object Aggregates {}

  object Artifacts {
    object Util {
      val Id = ArtifactId("util")
      val Art = Artifact(
        name = Id,
        libs = Seq(
          //Later we can move this defs to utils. As now you use `TechRadar.WhiskUtil.ZioApp`
          Whisk.Libraries.Util("zio-app"),
          Whisk.Libraries.Util("zio-config")
        ),
        depends = Seq()
      )
    }

    object Fridge {
      val Id = ArtifactId("fridge")
      val Art = Artifact(
        name = Id,
        libs = Seq.empty,
        depends = Seq(
          Util.Id
        )
      )
    }

    object Cooktop {
      val Id = ArtifactId("cooktop")
      val Art = Artifact(
        name = Id,
        libs = Seq(
          Whisk.Libraries.Util("locale")
        ),
        depends = Seq(Util.Id)
      )
    }
  }

  object Plugins {
    val Util = SbtPlugin(
      "com.whisk",
      "whisk-sbt-plugin",
      Version.VExpr("PV.whiskSbtPlugin")
    )
  }
}

object Sbt {
  val settings = GlobalSettings(
    groupId = "me.fidonode",
    sbtVersion = None
  )

  def aggregate(artifacts: Seq[Artifact]) = Aggregate(
    name = ArtifactId("modules"),
    artifacts = artifacts,
    pathPrefix = Seq("module"),
    groups = Set(Group("sbtgen-poc")),
    defaultPlatforms = Seq(
      PlatformEnv(
        platform = Platform.Jvm,
        language = Seq(ScalaVersion("2.13.15"))
      )
    ),
    enableProjectSharedAggSettings = true
  )

  def rootProject(projection: Projection) = Project(
    name = ArtifactId("sbtgen-poc"),
    aggregates = projection.aggregates,
    sharedSettings = Seq(
      "libraryDependencySchemes" ++= Whisk.Settings.circeIsDigusting
    ),
    topLevelSettings = Seq(
      "libraryDependencies" += """"io.7mind.izumi.sbt" %% "sbtgen" % "0.0.104"""".raw
    ),
    rootPlugins = Plugins(
      enabled = Seq(
        Plugin("SbtgenVerificationPlugin")
      )
    ),
    appendPlugins = Defaults.SbtGenPlugins ++ Seq(
      Whisk.Plugins.Util
    )
  )
}
