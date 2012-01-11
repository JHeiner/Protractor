
// Copyright © 2011-2012, Jeremy Heiner (github.com/JHeiner). All rights reserved.
// Copying and distribution of this file, with or without modification, are
// permitted in any medium without royalty provided the copyright notice and this
// notice are preserved.  This file is offered as-is, without any warranty.

name := "protractor"

libraryDependencies ++= Seq(
	"org.scala-tools.testing" %% "scalacheck" % "1.9" % "test" ,
	"org.specs2" %% "specs2" % "1.6.1" % "test" ,
	"org.specs2" %% "specs2-scalaz-core" % "6.0.1" % "test" ,
	"junit" % "junit" % "4.7" % "test" )

scalaSource in Test <<= (thisProject) { p => new File(p.base,"test") }

