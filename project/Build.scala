
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

object Build extends sbt.Build
{
  import sbt._

  lazy val root = settings( Project("root", file(".")) aggregate(lib,gui) )
  lazy val lib = settings(  Project("lib", file("lib")) )
  lazy val gui = settings(  Project("gui", file("gui")) dependsOn(lib) )

  import Keys._
  import java.io.File

  def settings( p:Project ) = p.settings(
	// no need to litter...
	target <<= (baseDirectory in ThisBuild,thisProject) {
	  (b,p) => b / ("target/"+p.id) }
	,
	// collect the dependencies for eclipse...
	update <<= (update,target) map {
	  (u,t) => {
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
	)
}
