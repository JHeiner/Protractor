
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

package protractor.test

import protractor._
import org.specs2._
import org.specs2.matcher.{Matcher,Expectable,DataTables}
import org.scalacheck._
import java.lang.Math.{PI,abs,atan2,cos,sin,sqrt,getExponent,pow}
import java.lang.Double.MIN_EXPONENT
import protractor.minutiae.{Samples,SeqDouble,SeqFloat}
import protractor.minutiae.SeqReal.toSeqReal
//import protractor.minutiae.MouseEventBufferStroke
//import java.awt.event.MouseEvent.{MOUSE_PRESSED,MOUSE_DRAGGED,MOUSE_RELEASED}
import scala.collection.immutable.TreeMap

object Spec
{
  def main( arguments:Array[String] ) {
    specs2.run.main( "protractor.test.Spec" +: arguments ) }
}
class Spec extends Specification with ScalaCheck with DataTables
{
  type XYArray = Array[Double]

  def isNearZero( v:Float ):Boolean =
	-6.1e-5 <= v && v <= 6.1e-5
  def isNearZero( v:Double ):Boolean =
	-1e-10 <= v && v <= 1e-10

  def isNear( x:Float, n:Float ):Boolean =
	if ( isNearZero( x ) )
	  isNearZero( n )
	else if ( isNearZero( n ) )
	  false
	else {
	  val en = getExponent(n)
	  val ex = getExponent(x)
	  val xe = en max ex
	  val ne = en min ex
	  if ( xe == ne || xe == 1 + ne ) {
		val d = pow( 2, MIN_EXPONENT max (xe-13) )
		( n - d <= x ) && ( x <= n + d ) }
	  else false }
  def isNear( x:Double, n:Double ):Boolean =
	if ( isNearZero( x ) )
	  isNearZero( n )
	else if ( isNearZero( n ) )
	  false
	else {
	  val en = getExponent(n)
	  val ex = getExponent(x)
	  val xe = en max ex
	  val ne = en min ex
	  if ( xe == ne || xe == 1 + ne ) {
		val d = pow( 2, MIN_EXPONENT max (xe-33) )
		( n - d <= x ) && ( x <= n + d ) }
	  else false }

  def beNear( n:Float ):Matcher[Float] =
	new Matcher[Float] {
	  def apply[S<:Float]( ed:Expectable[S] ) = {
		val x = ed.value
		result( isNear( x, n ),
			   x +" is close to "+ n,
			   x +" is not close to "+ n,
			   ed) } }
  def beNear( n:Double ):Matcher[Double] =
	new Matcher[Double] {
	  def apply[S<:Double]( ed:Expectable[S] ) = {
		val x = ed.value
		result( isNear( x, n ),
			   x +" is close to "+ n,
			   x +" is not close to "+ n,
			   ed) } }

  def beNear( a:Array[Float] ):Matcher[Array[Float]] =
	new Matcher[Array[Float]] {
	  def describe( ad:Array[Float] ) = ad.mkString("[",", ","]")
	  def apply[S<:Array[Float]]( ead:Expectable[S] ) = {
		val x = ead.value
		result( x zip a forall { t => isNear( t._1, t._2 ) },
			   describe(x) + " is close to "+describe(a),
			   describe(x) + " is not close to "+describe(a),
			   ead) } }
  def beNear( a:Array[Double] ):Matcher[Array[Double]] =
	new Matcher[Array[Double]] {
	  def describe( ad:Array[Double] ) = ad.mkString("[",", ","]")
	  def apply[S<:Array[Double]]( ead:Expectable[S] ) = {
		val x = ead.value
		result( x zip a forall { t => isNear( t._1, t._2 ) },
			   describe(x) + " is close to "+describe(a),
			   describe(x) + " is not close to "+describe(a),
			   ead) } }

  def rotateSinCos( s:Double, c:Double, xy:Array[Double] ) =
	toSeqReal(xy) rotXY(s,c) toArray

  def rotate( r:Double, xy:Array[Double] ) =
	rotateSinCos( sin(r), cos(r), xy )

  def dot( one:XYArray, two:XYArray ) =
	//toSeqReal(one) dot two
	SeqDouble.dot( one, two )

  def mm( xy:XYArray ) = dot(xy,xy)

  def xy( a:Double* ) = a.toArray

  def xyGen = Arbitrary(Gen.containerOfN[Array,Double](2*Samples,Gen.choose(-1000d,1000d)))

  def rGen = Arbitrary(Gen.choose(-2*PI,2*PI))

  @scala.annotation.tailrec
  final def angle( r:Double ):Double =
	if ( r > PI ) angle( r - 2*PI ) else
	if ( r <= -PI ) angle( r + 2*PI ) else
	r // atan2 only returns -PI when given a negativezero

  def is =
  "Protractor library parts should behave as follows:" ^
  "dot should produce these known results" ! {
	"one"       | "two"       | "output" |
	xy(0,1,0,1) ! xy(1,0,1,0) ! 0        |
	xy(3,4)     ! xy(3,4)     ! 25       |
	xy(-3,4)    ! xy(-3,4)    ! 25       |
	xy(-3,4)    ! xy(3,-4)    ! -25      |
	xy(-3,4)    ! xy(3,-4)    ! -25      |
	xy(3,-4)    ! xy(3,4)     ! -7       |
	xy(3,4)     ! xy(3,-4)    ! -7       |
	xy(3,-4)    ! xy(-3,-4)   ! 7        |
	xy(-3,-4)   ! xy(3,-4)    ! 7        |
	xy(-3,5,11) ! xy(7,3,2)   ! 16       |
	xy(3,5,11)  ! xy(7,3,-2)  ! 14       |> {
	  (one,two,out) => dot(one,two) must beNear(out) }
  }^
  "rotate should produce these known results" ! {
	"angle" | "input"     | "output"      |
	0d      ! xy()        ! xy()          |
	0d      ! xy(1,0,0,1) ! xy(1,0,0,1)   |
	PI/2    ! xy(1,0,0,1) ! xy(0,1,-1,0)  |
	PI      ! xy(1,0,0,1) ! xy(-1,0,0,-1) |
	-PI     ! xy(1,0,0,1) ! xy(-1,0,0,-1) |
	-PI/2   ! xy(1,0,0,1) ! xy(0,-1,1,0)  |
	2*PI    ! xy(1,0,0,1) ! xy(1,0,0,1)   |
	PI/6    ! xy(2,0)     ! xy(sqrt(3),1) |
	PI/3    ! xy(2,0)     ! xy(1,sqrt(3)) |> {
	  (ang,in,out) => rotate(ang,in) must beNear(out) }
  }^
  "all the Double dot implementations are close" ! {
	(xyGen,xyGen)( (one:XYArray,two:XYArray) =>
	  (one.length == 2*Samples) ==> // prevents shrinkage
	  (two.length == 2*Samples) ==> // prevents shrinkage
		{ val beNearDot = beNear( SeqDouble.dotKahan( one, two ) )
		 ((SeqDouble.dot(one,two) must beNearDot) &&
		  (toSeqReal(one) dot two  must beNearDot) &&
		  (toSeqReal(one) dotKahan two  must beNearDot)) } )
  }^
  "all the Float dot implementations are close (sometimes fails, rerun)" ! {
	(xyGen,xyGen)( (one:XYArray,two:XYArray) =>
	  (one.length == 2*Samples) ==> // prevents shrinkage
	  (two.length == 2*Samples) ==> // prevents shrinkage
		{ val af1 = ( toSeqReal(one) toFloat ) toArray
		  val af2 = ( toSeqReal(two) toFloat ) toArray
		  val beNearDot = beNear( SeqFloat.dotKahan( af1, af2 ) )
		 ((SeqFloat.dot(af1,af2) must beNearDot) &&
		  (SeqFloat.dotDouble(af1,af2).toFloat must beNearDot) &&
		  (SeqFloat.dotKahanDouble(af1,af2).toFloat must beNearDot) &&
		  (toSeqReal(af1) dot af2 must beNearDot) &&
		  (toSeqReal(af1) dotKahan af2 must beNearDot) &&
		  ((toSeqReal(af1) dotDouble af2).toFloat must beNearDot)) } )
  }^
  "for float data, Double and Float dot implementations are close" ! {
	(xyGen,xyGen)( (one:XYArray,two:XYArray) =>
	  (one.length == 2*Samples) ==> // prevents shrinkage
	  (two.length == 2*Samples) ==> // prevents shrinkage
		{ val af1 = ( toSeqReal(one) toFloat ) toArray
		  val af2 = ( toSeqReal(two) toFloat ) toArray
		  val ad1 = ( toSeqReal(af1) toDouble ) toArray
		  val ad2 = ( toSeqReal(af2) toDouble ) toArray
		  val beNearDot = beNear( SeqDouble.dotKahan( ad1, ad2 ) )
		 ((SeqFloat.dotKahanDouble(af1,af2) must beNearDot)) } )
  }^
  "sqDiff should produce these known results" ! {
	"one"       | "two"       | "output" |
	xy(0,1,0,1) ! xy(1,0,1,0) ! 4        |
	xy(3,4)     ! xy(3,4)     ! 0        |
	xy(-3,5,1)  ! xy(7,9,2)   ! 117      |> {
	  (one,two,out) => toSeqReal(one) sqDiff two must beNear(out) }
  }^
  "exercize Double sqDiff implementations" ! {
	(xyGen,xyGen)( (one:XYArray,two:XYArray) =>
	  (one.length == 2*Samples) ==> // prevents shrinkage
	  (two.length == 2*Samples) ==> // prevents shrinkage
		(toSeqReal(one) sqDiffKahan two must
		 beNear( toSeqReal(one) sqDiff two )) )
  }^
  "rotate should not change the magnitude" ! {
	(xyGen,rGen)( (xy:XYArray,r:Double) =>
	  (r != 0) ==>
	  (xy.length == 2*Samples) ==> // prevents shrinkage
		(mm(rotate(r,xy)) must beNear(mm(xy))) )
  }^
  "dot should be the cosine" ! {
	(xyGen,rGen)( (xy:XYArray,r:Double) =>
	  (r != 0) ==>
	  (xy.length == 2*Samples) ==> { // prevents shrinkage
		val tr = rotate(r,xy)
		sqrt(mm(xy)*mm(tr))*cos(r) must beNear(dot(xy,tr)) })
  }^
  "closed-form solution finds optimal angle" ! {
	(xyGen,rGen)( (xy:XYArray,r:Double) =>
	  (r != 0) ==>
	  (xy.length == 2*Samples) ==> { // prevents shrinkage
		val tr = rotate(r,xy)
		val denom = dot(xy,tr)
		val numer = dot(xy,rotateSinCos(-1,0,tr))
		angle(r) must beNear(atan2(numer,denom)) })
  }^ /*
  "mouse event stroke sampling should produce these known results" ! {
	"n" | "input"          | "output"            |
	3   ! Seq(0,0,0,1,1,1) ! xy(0,0,0.5,0.5,1,1) |
	3   ! Seq(0,0,0,1,1,0) ! xy(0,0,0.5,0.5,1,1) |
	3   ! Seq(0,0,0,1,1,8) ! xy(0,0,0.5,0.5,1,1) |
	3   ! Seq(0,0,0,0,0,4,1,1,6,1,1,10) ! xy(0,0,0.5,0.5,1,1) |> {
	  (n,in,out) => {
		val buf = new MouseEventBuffer
		for ( (x::y::t::Nil) <- in.grouped(3) ) buf.add(x,y,t)
		buf.toXY(n).toArray must beNear(out) } }
  }^
  "mouse event stroke optimized sampling should match known results" ! {
	"input"          |
	Seq(0,0,0,1,1,1) |
	Seq(0,0,0,1,1,0) |
	Seq(0,0,0,1,1,8) |
	Seq(0,0,0,0,0,4,1,1,6,1,1,10) |> {
	  (in) => {
		val buf = new MouseEventBuffer
		for ( (x::y::t::Nil) <- in.grouped(3) ) buf.add(x,y,t)
		buf.toXY(Samples).toArray must beNear(buf.sample.toArray) } }
  }^
  "mouse event buffer optimized sampling should match functional results" ! {
	def generate( p:Gen.Params ):Option[MouseEventBuffer] = {
	  val buffer = new MouseEventBuffer
	  var when = 0
	  def rngCoord = p.rng.nextInt(401) - 200
	  def addEvent( id:Int ) {
		buffer.add( rngCoord, rngCoord, when )
		when = when + p.rng.nextInt(10) }
	  addEvent( MOUSE_PRESSED )
	  for ( item <- -15 to p.rng.nextInt(60) )
		addEvent( MOUSE_DRAGGED )
	  addEvent( MOUSE_RELEASED )
	  Some(buffer) }
	(Arbitrary(Gen(generate)))( (buf:MouseEventBuffer) => {
	  buf.toXY(Samples).toArray must beNear(buf.sample.toArray) })
  }^ */
  end
}

/*

case class MouseEventItem( x:Double, y:Double, t:Double )
{
  def i( a:Double, u:Double, b:Double ) = a + u * (b - a)
  def interp( o:MouseEventItem, t:Double ) = {
	val u = (t - this.t) / (o.t - this.t)
	MouseEventItem( i(x,u,o.x), i(y,u,o.y), t ) }

  def toTabbedColumns = "\t"+x+"\t"+y+"\t"+t
}

class MouseEventStroke extends MouseEventBufferStroke
{
  def toTuple( i:Int ) = { (xs(i),ys(i),ts(i)) }
  def toTuples = for ( i <- 0 until size ) yield toTuple(i)
  def grouped = TreeMap( toTuples groupBy { _._3 } toSeq :_* )
  def unclumped = grouped mapValues { v => {
	val d = 1.0 / v.size
	for ( ((x,y,t),i) <- v.zipWithIndex )
	yield MouseEventItem(x,y,t+d*i) } }
  def times( n:Int, u:Map[Int,Seq[MouseEventItem]] ) = {
	val t0 = u.head._2.head.t ; val tn = u.last._2.last.t
	val d = ( tn - t0 )/( n - 1 )
	val ts = for ( i <- 1 to (n - 2) ) yield t0 + d * i
	t0 +:( ts ):+ tn }
  def sampled( n:Int ) = {
	val u = unclumped
	val v = u.valuesIterator.flatten.toList
	var z = v zip v.tail
	for ( t <- times(n,u) ) yield {
	  z = z dropWhile { _._2.t < t }
	  val (i0,i1) = z.head ; i0.interp( i1, t ) } }
  def toXY( n:Int ) = {
	val is = sampled(n)
	val ps = for ( MouseEventItem(x,y,t) <- is ) yield Seq(x,y)
	ps.flatten }

  def toTabbedColumns =
	toTuples map { _.productIterator.mkString("\t","\t","") }
  def toRows = toTabbedColumns.mkString("\n")
  def unclumpedTabbedColumns =
	unclumped.valuesIterator.flatten.map{ _.toTabbedColumns }
  def unclumpedRows = unclumpedTabbedColumns.mkString("\n")
}

  def sample( stroke:List[Point] ):SampledStroke =
	if ( stroke.lengthCompare(Samples) == 0 )
	  stroke.toArray
	else stroke match {
	case Nil => throw new IllegalArgumentException( "empty" )
	case one :: Nil => Array(one)
	case head :: tail => {
	  val accum = new AccumulateInterpolationData( head )
	  var data = tail map accum
	  val step = accum.total / (Samples-1)
	  (for ( i <- 0 until Samples ) yield
		if (i == 0) data.head.p1
		else if (i == Samples-1) accum.last
		else { val d = step * i
		  data = data dropWhile { _.d2 <= d }
		  data.head.interpolate(d)
			}).toArray } }

  def validate( raw:Seq[List[Point]] ):Seq[TransposedStroke] = {
	val sampled:Seq[SampledStroke] = raw map sample
	val offcenter:Seq[TransposedStroke] = sampled map { _.transpose }
	val count:Int = sampled map { _.size } sum
	val centroid:Seq[Coordinate] = offcenter.transpose map { _.flatten } map { _.sum } map { _ / count }
	val centered:Seq[TransposedStroke] = for ( d <- offcenter ) yield
	  for ( (a,c) <- d zip centroid ) yield a map { _ - c }
	val furthest:Double = java.lang.Math.sqrt( centered map { _.transpose map { v => dot(v,v) } max } max )
	val scaled:Seq[TransposedStroke] = centered map { _ map { _ map { _ / furthest } } }
	scaled }

  class AccumulateInterpolationData( p:Point )
  extends (Point => InterpolationData)
  {
	import SeqReal.toSeqReal
	var total:Double = 0
	var last = p
	def apply( next:Point ) = {
	  val before = total
	  val after = before + java.lang.Math.sqrt(toSeqReal(last) sqDiff next)
	  val data = InterpolationData(last,next,before,after)
	  total = after
	  last = next
	  data }
  }

  case class InterpolationData( p1:Point, p2:Point, d1:Double, d2:Double )
  {
	def interpolate( d:Double ):Point = {
	  assert( d1 <= d && d <= d2, "wrong segment: "+ d1 +" < "+ d +" < "+ d2 )
	  val u = (d - d1) / (d2 - d1)
	  assert( 0 <= u && u < 1 )
	  (p1 zip p2) map { p => p._1 + u * (p._2 - p._1) } }
  }

  def deepToString(a:Any):String =
	if ( a.isInstanceOf[Array[_]] )
	  a.asInstanceOf[Array[_]].map(deepToString).mkString(" [",",","] ")
	else if ( a.isInstanceOf[Traversable[_]] )
	  a.asInstanceOf[Traversable[_]].map(deepToString).toString
	else a.toString


*/
