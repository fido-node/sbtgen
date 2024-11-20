# sbtgen POC

## Prerequisites

You need either ammonite or coursier to run this script.

If you have coursier everything will simply work.

If you have ammonite only, you should call `amm ./sbtgen.sc <projection>` instead of `./sbtgen.sc <projection>`.

It is also may be a time to check nix + direnv + nix-direnv. I've placed regular nix and direnv files to this repo.
Check https://zero-to-nix.com/concepts/nix-installer
and https://github.com/direnv/direnv/blob/master/docs/installation.md

I use nix and direnv for daily bootstrapping my dev env for all our repos with `flake.nix` file.

Also place this to ` ~/.sbt/1.0/credentials.sbt`

```sbt
// Make idea resolve sbtgen imports in project/SbtAutomation.sc
libraryDependencies += "io.7mind.izumi.sbt" %% "sbtgen" % "0.0.104"

// Pure workaround
credentials += Credentials(Path.userHome / ".m2" / ".credentials")
resolvers += "internal.repo.read" at "https://nexus.whisk-dev.com/repository/whisk-maven-group/"

```

Lazy temporary workaround, cause sbtgen can't write random things to `project/plugins.sbt`.

## Usage

By default, I've commited `buidl.sbt` generated `./sbtgen.sc all`.
So you can import this project into idea and see 3 modules: cooktop, fridge, and util.

After that you can run `./sbtgen.sc fridge` to generate `build.sbt` for fridge and util modules.

Same with `./sbtgen.sc cooktop` to generate `build.sbt` for cooktop and util modules.

And at last `./sbtgen.sc util` to generate `build.sbt` for util module only.

Depending on your configuration Idea may reimport sbt project after each scrip invocation.

I also have `Global / onChangedBuildSource:= ReloadOnSourceChanges` in `~/.sbt/1.0/global.sbt`. So if I have CLI sbt
process, it will reload settings before processing next command.

That is a main idea. Narrow down list of modules to make build and reload faster.

## How it works

You run `./sbtgen.sc all` as entrypoint.

```scala worksheet
//Add bash shenaigans.
#!/ bin / sh
  //Install ammonite through coursier and run this script through ammonite.
  //Yo, dawg, recursion in your shell-scala-script-whatever-mess
  cs launch com.lihaoyi:ammonite_2.13.13:3.0.0-M1 --fork -M ammonite.Main -- sbtgen.sc $*
exit
!#
//Import code from project/SbtAutomation.sc and import everything from it flattened.

import $file.project.SbtAutomation, SbtAutomation._

@main
//Call our code from SbtAutomation.sc/Main/entrypoint
def entrypoint(args: String*) = Main.entrypoint(args)
```

Most of the magic happens in `project/SbtAutomation.sc` file.

We describe a build with a quite handy DSL.
A little bit verbose and have additional overhead after rendering.
Not sure that I have something against it if we can hide all regular sbt DSL under the hood.
We may adopt parts of this DSL in our utils plugin to make integration easier.
Also, I assume that we may change build system by implementing custom renderer for its syntax.

We define set of artifacts required for desired projection and throw them into `Entrypoint.main` method.

## Conclusions

- It is an extremely simplified example
- Even with DSL and compile time checks, it still has weird unchecked string->string mappings.

```scala
Version.VExpr(
  "PV.whiskSbtPlugin"
) // linked to PV object in project/project/PluginVersions.scala
Version.VExpr(
  "V.whiskSbtPlugin"
) // linked to V object in project/Versions.scala
```

These mappings limit object names and not file names.
Also, they are scoped.
If you want to use constant in `build.sbt`, you should place your file with object under `/project`
If you want to use constant in `project/plugins.sbt`, you should place your file with object under `/project/project`

- We have a whopping 3300 lines of sbt shit in `*.sbt` files in atlas
- It is easy to start writing full-blown Scala code in `*.sc` files
- For now, you can place only `addSbtPlugin` into `project/plugins.sbt` with sbtgen.
- I have no idea what all these things about `.agg` doing and what is the cost of it. 