
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

  class ImmutableValArray[A<:AnyVal]( source:Array[A] )
	extends scala.collection.immutable.IndexedSeq[A]
  {
	private val array = source.clone
	def length = array.length
	def apply( index:Int ) = array(index)
  }
  object ImmutableValArray
  {
	def apply[A<:AnyVal]( array:Array[A] ) = new ImmutableValArray( array )
	def empty[A<:AnyVal:ClassManifest] = apply( Array.empty[A] )
	implicit def fromSeq[A<:AnyVal:ClassManifest]( seq:TraversableOnce[A] ) =
	  apply( seq.toArray )
  }
  class ImmutableRefArray[A<:Immutable]( source:Array[A] )
	extends scala.collection.immutable.IndexedSeq[A]
  {
	private val array = source.clone
	def length = array.length
	def apply( index:Int ) = array(index)
  }
  object ImmutableRefArray
  {
	def apply[A<:Immutable]( array:Array[A] ) = new ImmutableRefArray( array )
	def empty[A<:Immutable:ClassManifest] = apply( Array.empty[A] )
	implicit def fromSeq[A<:Immutable:ClassManifest]( seq:TraversableOnce[A] ) =
	  apply( seq.toArray )
  }

  import scala.annotation.tailrec
  import SeqReal.toSeqReal

  object SeqReal
  {
	implicit def toSeqReal[T]( seq:Seq[T] ):SeqReal[T] =
	  new SeqReal[T]( seq )
  }

  class SeqReal[T]( val seq:Seq[T] )
  {
	def toFloat( implicit ops:Numeric[T] ):Seq[Float] =
	  for ( item <- seq ) yield ops.toFloat( item )

	def toDouble( implicit ops:Numeric[T] ):Seq[Double] =
	  for ( item <- seq ) yield ops.toDouble( item )

	def zipMinus( that:Seq[T] )( implicit ops:Numeric[T] ):Seq[T] =
	  for ( pair <- seq zip that ) yield ops.minus( pair._1, pair._2 )

	def zipTimes( that:Seq[T] )( implicit ops:Numeric[T] ):Seq[T] =
	  for ( pair <- seq zip that ) yield ops.times( pair._1, pair._2 )

	def squareEach( implicit ops:Numeric[T] ):Seq[T] =
	  for ( item <- seq ) yield ops.times( item, item )

	def sumKahan( implicit ops:Numeric[T] ):T = {
	  var sum:T = ops.fromInt( 0 )
	  var carry:T = ops.fromInt( 0 )
	  for ( item <- seq ) {
		val value = ops.minus( item, carry )
		val total = ops.plus( sum, value )
		carry = ops.minus( ops.minus( total, sum ), value)
		sum = total }
	  sum }

	def dotSelf( implicit ops:Numeric[T] ):T =
	  this.squareEach(ops) sum

	def dotSelfKahan( implicit ops:Numeric[T] ):T =
	  this.squareEach(ops) sumKahan

	def dotSelfDouble( implicit ops:Numeric[T] ):Double =
	  this.toDouble(ops) dotSelf

	def dot( that:Seq[T] )( implicit ops:Numeric[T] ):T =
	  this.zipTimes(that)(ops) sum

	def dotKahan( that:Seq[T] )( implicit ops:Numeric[T] ):T =
	  this.zipTimes(that)(ops) sumKahan

	def dotDouble( that:Seq[T] )( implicit ops:Numeric[T] ):Double =
	  this.toDouble(ops) dot that.toDouble(ops)

	def sqDiff( that:Seq[T] )( implicit ops:Numeric[T] ):T =
	  this.zipMinus(that)(ops) dotSelf

	def sqDiffKahan( that:Seq[T] )( implicit ops:Numeric[T] ):T =
	  this.zipMinus(that)(ops) dotSelfKahan

	def sqDiffDouble( that:Seq[T] )( implicit ops:Numeric[T] ):Double =
	  this.toDouble(ops) sqDiff that.toDouble(ops)

	def rotXY( sin:Double, cos:Double )( implicit ops:Numeric[T] ):Seq[Double] =
	  ( for ( xy <- this.toDouble(ops) grouped 2 ) yield xy match {
		  case Seq(x,y) => Seq( cos * x - sin * y, sin * x + cos * y )
		  case _ => throw new AssertionError("Seq pattern not matched?") }
	  ).flatten.toSeq
  }
  object SeqDouble
  {
	type T = Double
	// BEGIN copy+paste >>>>>>>>>>>>>>>>

	def dot( one:Array[T], two:Array[T] ) = {
	  @tailrec def dp( i:Int, sum:T ):T =
		if ( 0 > i ) sum else
		  dp( i - 1, sum + one(i) * two(i) )
	  dp( one.length - 1, 0 ) }

	def dotKahan( one:Array[T], two:Array[T] ) = {
	  @tailrec def dp( i:Int, carry:T, sum:T ):T =
		if ( 0 > i ) sum else {
		  val value = one(i) * two(i) - carry
		  val total = sum + value
		  dp( i - 1, (total - sum) - value, total ) }
	  dp( one.length - 1, 0, 0 ) }

	// <<<<<<<<<<<<<<<< copy+paste END
  }

  object SeqFloat
  {
	type T = Float
	// BEGIN copy+paste >>>>>>>>>>>>>>>>

	def dot( one:Array[T], two:Array[T] ) = {
	  @tailrec def dp( i:Int, sum:T ):T =
		if ( 0 > i ) sum else
		  dp( i - 1, sum + one(i) * two(i) )
	  dp( one.length - 1, 0 ) }

	def dotKahan( one:Array[T], two:Array[T] ) = {
	  @tailrec def dp( i:Int, carry:T, sum:T ):T =
		if ( 0 > i ) sum else {
		  val value = one(i) * two(i) - carry
		  val total = sum + value
		  dp( i - 1, (total - sum) - value, total ) }
	  dp( one.length - 1, 0, 0 ) }

	// <<<<<<<<<<<<<<<< copy+paste END

	def dotDouble( one:Array[T], two:Array[T] ):Double = {
	  @tailrec def dp( i:Int, sum:Double ):Double =
		if ( 0 > i ) sum else
		  dp( i - 1, sum + one(i).toDouble * two(i).toDouble )
	  dp( one.length - 1, 0 ) }

	def dotKahanDouble( one:Array[T], two:Array[T] ):Double = {
	  @tailrec def dp( i:Int, carry:Double, sum:Double ):Double =
		if ( 0 > i ) sum else {
		  val value = one(i).toDouble * two(i).toDouble - carry
		  val total = sum + value
		  dp( i - 1, (total - sum) - value, total ) }
	  dp( one.length - 1, 0, 0 ) }
  }
