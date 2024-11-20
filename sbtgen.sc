#!/bin/sh
  cs launch com.lihaoyi:ammonite_2.13.13:3.0.0-M1 --fork -M ammonite.Main -- sbtgen.sc $*
exit
!#
import $file.project.SbtAutomation, SbtAutomation._

@main
def entrypoint(args: String*) = Main.entrypoint(args)