
// Copyright © 2011-2012, Jeremy Heiner (github.com/JHeiner).
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
//   1. Redistributions of source code must retain the above copyright notice,
//      this list of conditions and the following disclaimer.
// 
//   2. Redistributions in binary form must reproduce the above copyright
//      notice, this list of conditions and the following disclaimer in the
//      documentation and/or other materials provided with the distribution.
// 
//   3. Neither the name of the copyright holder nor the names of any
//      contributors may be used to endorse or promote products derived from
//      this software without specific prior written permission.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

name := "protractor"

version := "0.3.1"

scalaVersion := "2.9.1"

scalacOptions ++= Seq("-optimize","-Xlint","-deprecation","-unchecked")

libraryDependencies ++= Seq(
	"org.scala-tools.testing" %% "scalacheck" % "1.9" % "test" ,
	"org.specs2" %% "specs2" % "1.6.1" % "test" ,
	"org.specs2" %% "specs2-scalaz-core" % "6.0.1" % "test" ,
	"junit" % "junit" % "4.7" % "test" )

//run in Compile <<= run in Compile in "gui"
//fork := true

update <<= (update,target) map { (u,t) => {
	// collect the dependencies for eclipse...
	for ( c <- u.configurations ) {
	  val dir = t / ("ivyresolved-"+c.configuration)
	  IO.delete(dir)
	  val map = new scala.collection.mutable.HashMap[String,File]
	  for ( m <- c.modules ; (a,file) <- m.artifacts ) {
	    if ( ! file.getName.equals( "scala-library.jar" ) ) {
	      require( ! map.contains( file.getName ),
	               "duplicate: "+ file +" and "+ map(file.getName) )
	      map.put( file.getName, file ) } }
	  if ( ! map.isEmpty ) IO.createDirectory(dir)
	  map.values foreach { file =>
	       IO.copyFile( file, new File( dir, file.getName ) ) } }
	u } }
