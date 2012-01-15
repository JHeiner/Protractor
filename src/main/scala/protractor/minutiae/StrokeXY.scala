
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

package protractor.minutiae

import protractor._
import scala.annotation.tailrec

case class StrokeXY( pauseBefore:Time,
		    interleaved:Interleaved, duration:Time ) extends Stroke
{
  def dimension = 2 ; def xy = interleaved
  require( xy.size == 2 * Samples, "stroke is wrong length" )
  val yNx = new IndexedSeq[Coordinate] {
    def length = xy.length ; def apply( index:Int ) =
      if ( (index & 1 ) == 0 ) xy( index + 1 ) else - xy( index - 1 ) }
  val mm = thisDot(interleaved)
  def optimal( fixed:StrokeXY ):Angle =
    java.lang.Math.atan2( thisDot(fixed.yNx), thisDot(fixed.xy) )
  def thisDot( that:Interleaved ):Double = {
    var sum = 0.0 ; val one = interleaved ; val two = that
    var i = one.length ; require( i == two.length, "unexpected length" )
    while ( i > 0 ) { i -= 1 ; sum += one(i) * two(i) }
    sum }
  def compareAt( sin:Double, cos:Double, fixed:StrokeXY ):Similarity =
    if ( mm == 0 && fixed.mm == 0 ) 1 else {
      @tailrec def cmp( i:Int, prod:Double, dist:Double ):Similarity =
	if ( 0 <= i ) {
	  val f = fixed.xy(i) ; val r =
	    if ( (i & 1 ) == 0 ) cos*xy(i) - sin*xy(i+1)
	    else sin*xy(i-1) + cos*xy(i)
	  val p = r * f ; val d = r - f
	  cmp( i - 1, prod + p, dist + d*d ) }
	    else {
	      val dot = prod * prod / mm / fixed.mm
	      val sim = if ( java.lang.Double.isNaN(dot) ) 0
			else java.lang.Math.sqrt( dot / ( 1 + dist/1000 ))
	      //println( "da: sqrt("+dot+" /1+ "+dist+") = "+sim )
	      if ( sim > 1 ) 1 else sim } // can have tiny rounding errors
      cmp( 2*Samples - 1, 0.0, 0.0 ) }
  def sumXY:Array[Coordinate] =
    xy.grouped(2).toList.transpose.map{_.sum}.toArray
  def rotate( sin:Double, cos:Double ) =
    if ( sin==0 && cos==1 ) this else
    StrokeXY(pauseBefore,rotXY(sin,cos),duration)
  def rotXY( sin:Double, cos:Double ):Interleaved = {
    val a = Array.ofDim[Double](2*Samples)
    val b = interleaved
    var i = b.length ; while ( i > 0 ) {
      i -= 1 ; val y = b(i) ; i -= 1 ; val x = b(i)
      a(i+0) = cos * x - sin * y
      a(i+1) = sin * x + cos * y }
    ImmutableValArray(a) }
  override def toString =
    ( if (pauseBefore == 0) "" else "Pause("+pauseBefore+")" )+
  xy.mkString( "StrokeXY("+duration+", ",",",")" )
}

