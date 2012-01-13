
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

package protractor

package object minutiae
{
  val Samples = 32

  type Interleaved = ImmutableValArray[Coordinate]
  val InterleavedEmpty = ImmutableValArray.empty[Coordinate]
}
package minutiae
{
  import SeqReal.toSeqReal
  import scala.annotation.tailrec
  import scala.collection.immutable.IndexedSeq
  import scala.collection.mutable.ArrayBuffer
  import java.lang.Double.isNaN
  import java.lang.Math.sqrt

  sealed trait Stroke extends Immutable
  {
	def pauseBefore:Time
	require( pauseBefore >= 0, "negative pause" )

	def duration:Time
	require( duration >= 0, "negative duration" )

	def dimension:Int
	require( dimension == 2, "unsupported dimension: "+dimension )

	def interleaved:Interleaved
	require( interleaved.size % dimension == 0, "wrong coordinate count" )

	def mm:Double
  }
  case class StrokeXY( pauseBefore:Time,
		  interleaved:Interleaved, duration:Time ) extends Stroke
  {
	def dimension = 2 ; def xy = interleaved
	require( xy.size == 2 * Samples, "stroke is wrong length" )
	val yNx = new IndexedSeq[Coordinate] {
	  def length = xy.length ; def apply( index:Int ) =
		if ( (index & 1 ) == 0 ) xy( index + 1 ) else - xy( index - 1 ) }
	val mm = interleaved.dotSelf
	def optimal( fixed:StrokeXY ):Angle =
	  java.lang.Math.atan2( xy dot fixed.yNx, xy dot fixed.xy )
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
		  val sim = if ( isNaN(dot) ) 0 else sqrt( dot / ( 1 + dist/1000 ))
		  //println( "da: sqrt("+dot+" /1+ "+dist+") = "+sim )
		  if ( sim > 1 ) 1 else sim } // can have tiny rounding errors
	  cmp( 2*Samples - 1, 0.0, 0.0 ) }
	def sumXY:Array[Coordinate] =
	  xy.grouped(2).toList.transpose.map{_.sum}.toArray
	def rotate( sin:Double, cos:Double ) =
	  StrokeXY(pauseBefore,xy.rotXY(sin,cos),duration)
	override def toString =
	  ( if (pauseBefore == 0) "" else "Pause("+pauseBefore+")" )+
	  xy.mkString( "StrokeXY("+duration+", ",",",")" )
  }
  case class Rotation( angle:Double ) extends Immutable
  {
	def limited( to:Double ) = {
	  val max = to ; val min = - max
	  if ( angle < min ) Rotation(min)
	  else if ( max < angle ) Rotation(max)
	  else this }

	def apply( strokes:StrokeSeq ):StrokeSeq =
	  strokes match { case that:SeqStrokeXY => {
	  val sin = java.lang.Math.sin(angle) ; val cos = java.lang.Math.cos(angle)
	  // TODO: new StrokeSeq(this.map(_.rotate(sin,cos)).toArray)
	  StrokeSeq(that map { _.rotate(sin,cos) } ) }}
  }
  object ZeroRotation extends Rotation(0)
  {
	override def limited( to:Double ) = this
	override def apply( strokes:StrokeSeq ):StrokeSeq = strokes
  }
  trait StrokeSeq extends IndexedSeq[Stroke]
  {
	def optimal( fixed:StrokeSeq ):Rotation
	def compareAt( r:Rotation, fixed:StrokeSeq ):Similarity
  }
  class SeqStrokeXY( a:Array[StrokeXY] )
  extends ImmutableRefArray(a) with StrokeSeq
  {
	def optimal( fixed:StrokeSeq ):Rotation =
	  if (size != fixed.size) ZeroRotation else
	  fixed match { case that:SeqStrokeXY => {
		val angles:Seq[Angle] =
		  for ( (t,o) <- this.zip(that) ) yield t.optimal(o)
		Rotation( angles.sum / angles.size ) }}

	def compareAt( r:Rotation, fixed:StrokeSeq ):Similarity =
	  if (size != fixed.size) 0 else
	  fixed match { case that:SeqStrokeXY => {
		val sin = java.lang.Math.sin(r.angle)
		val cos = java.lang.Math.cos(r.angle)
		val d = for ( (t,o) <- this.zip(that) ) yield t.compareAt(sin,cos,o)
		d.product }}
  }
  object StrokeSeq
  {
	implicit def apply( strokes:Seq[Stroke] ):StrokeSeq = {
	  val array = strokes.map(_.asInstanceOf[StrokeXY]).toArray
	  val centroid = (for ( s <- array ) yield s.sumXY).transpose.map{_.sum}
	  require( centroid.forall{isNearZero(_)}, "not centered" )
	  new SeqStrokeXY(array) }

	def isNearZero( v:Double ):Boolean =
	  -1e-10 <= v && v <= 1e-10
  }

  class MouseEventBufferStrokes
  {
	var firstPress:Long = 0
	var lastDelta:Int = 0
	val strokes = new ArrayBuffer[MouseEventBufferStroke]

	def startNewStroke( when:Long ) {
	  if ( strokes.isEmpty ) {
		firstPress = when ; lastDelta = 0 }
	  strokes += new MouseEventBufferStroke }

	def add( x:Int, y:Int, when:Long ) {
	  val delta = when - firstPress
	  if ( delta < lastDelta )
		throw new RuntimeException( "time going backwards?" )
	  if ( Int.MaxValue < delta )
		throw new RuntimeException( "stroke is too long" )
	  lastDelta = delta.toInt
	  strokes.last.add( x, y, lastDelta ) }

	def gesture = {
	  require( ! strokes.isEmpty, "no strokes" )
	  var cx:Coordinate = 0 ; var cy:Coordinate = 0
	  for ( s <- strokes ) { s.sample ; cx = cx + s.sx ; cy = cy + s.sy }
	  cx = cx / Samples / strokes.size ; cy = cy / Samples / strokes.size
	  for ( s <- strokes ) s.center(cx,cy)
	  val array = Array.ofDim[StrokeXY](strokes.size)
	  var last = 0.0
	  for ( i <- 0 until array.length ) {
		val next = strokes(i)
		array(i) = next.toStrokeXY( last )
		last = next.lastTime }
	  new Gesture( new SeqStrokeXY( array ), NoLimit ) }
  }
  class MouseEventBufferStroke
  {
	var size = 0
	var xs = Array.ofDim[Int](80)
	var ys = Array.ofDim[Int](80)
	var ts = Array.ofDim[Int](80)

	def clear() { size = 0 }

	def add( x:Int, y:Int, t:Int ) {
	  val i = size ; if ( i == xs.length ) {
		xs = grow(xs) ; ys = grow(ys) ; ts = grow(ts) }
	  size = i + 1 ; xs(i) = x ; ys(i) = y ; ts(i) = t }

	def grow( a:Array[Int] ) = {
	  val s = a.length
	  val b = Array.ofDim[Int]( 2 * s )
	  System.arraycopy( a, 0, b, 0, s )
	  b }

	def drawPolyline( g:java.awt.Graphics ) {
	  g.drawPolyline( xs, ys, size ) }

	val xy = Array.ofDim[Coordinate](2*Samples)
	var ds = Array.ofDim[Time](80)
	def lastTime = ds(size-1)
	def toStrokeXY( p:Time ) =
	  StrokeXY( ds(0)-p, ImmutableValArray(xy), ds(size-1)-ds(0) )

	var sx:Coordinate = 0 ; var sy:Coordinate = 0
	def center( cx:Coordinate, cy:Coordinate ) {
	  var i = 2*Samples - 1 ; while ( i > 0 ) {
		xy(i) = xy(i) - cy ; i = i - 1
		xy(i) = xy(i) - cx ; i = i - 1 } }

	def sample() {
	  val s = size - 1
	  if ( s < 0 )
		throw new AssertionError( "negative size" )
	  if ( s == 0 ) {
		var o = 2*Samples-1 ; val x = xs(0) ; val y = ys(0)
		while ( o >= 0 ) { xy(o) = y ; o = o-1 ; xy(o) = x ; o = o-1 }
		ds(0) = ts(0) ; sx = x*Samples ; sy = y*Samples }
	  else {
		if ( ds.length <= s ) ds = Array.ofDim[Time](ts.length)
		var i = 0 ; while ( i <= s ) {
		  val t = ts(i) ; ds(i) = t
		  var j = i + 1 ; while ( j <= s && t == ts(j) ) j = j + 1
		  val d = 1.0 / (j - i)
		  i = i + 1
		  var v:Time = t
		  while ( i < j ) {
			v = v + d ; ds(i) = v ; i = i + 1 } }
		val dt = (ds(s) - ds(0)) / (Samples - 1)
		sx = xs(0) ; sy = ys(0)
		var o = 0 ; var n = 0 ; i = 0
		xy(o) = sx ; o = o+1 ; xy(o) = sy ; o = o+1
		while ( o < 2 * Samples - 2 ) {
		  n = n+1 ; val t = ts(0) + dt * n
		  while ( t > ds(i+1) ) i = i + 1
		  val u = (t-ds(i))/(ds(i+1)-ds(i))
		  val x = xs(i) + u * (xs(i+1)-xs(i))
		  xy(o) = x ; o = o+1 ; sx = sx + x
		  val y = ys(i) + u * (ys(i+1)-ys(i))
		  xy(o) = y ; o = o+1 ; sy = sy + y }
		val x = xs(s) ; xy(o) = x ; sx = sx + x ; o = o+1
		val y = ys(s) ; xy(o) = y ; sy = sy + y } }
  }
}

