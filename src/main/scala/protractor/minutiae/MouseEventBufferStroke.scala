
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

class MouseEventBufferStroke
{
  // = the basic buffer storage =

  var size = 0
  // kinda like ResizableArray, but without all the auto-boxing-unboxing.
  var xs = Array.ofDim[Int](80) // x coords
  var ys = Array.ofDim[Int](80) // y coords
  var ts = Array.ofDim[Int](80) // times

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
    // drawPolyline is the obvious choice for providing feedback during
    // the user interaction: fastest available (maybe hardware accelerated).
    // this call would not be efficient without access to primitive arrays.
    g.drawPolyline( xs, ys, size ) }

  // = storage needed during conversion =

  var ds = Array.ofDim[Time](80) // times, but without duplicates
  val xy = Array.ofDim[Coordinate](2*Samples) // result of sampling
  var sx:Coordinate = 0 ; var sy:Coordinate = 0 // sum of the sampled coords

  // = conversion to stroke =
  // takes two steps because the centroid depends upon all the samples of
  // all the strokes in a gesture.

  // samples xs and ys uniformly in time into xy (not centered yet).
  // also sums the sampled coords into sx and sy (for centroid calculation).
  def sample() {
    val s = size - 1
    if ( s < 0 )
      throw new IllegalStateException( "no coordinates" )
    else if ( s == 0 ) {
      // one coordinate is a "dot" (won't happen if presses/releases paired)
      var o = 2*Samples-1 ; val x = xs(0) ; val y = ys(0)
      while ( o >= 0 ) { xy(o) = y ; o = o-1 ; xy(o) = x ; o = o-1 }
      ds(0) = ts(0) ; sx = x*Samples ; sy = y*Samples }
    else {
      if ( ds.length <= s ) ds = Array.ofDim[Time](ts.length)
      var i = 0 ; while ( i <= s ) {
        // spread out events that happen in single time slot
        val t = ts(i) ; ds(i) = t
        var j = i + 1 ; while ( j <= s && t == ts(j) ) j = j + 1
        val d = 1.0 / (j - i)
        i = i + 1
        var v:Time = t
        while ( i < j ) {
          v = v + d ; ds(i) = v ; i = i + 1 } }
      val dt = (ds(s) - ds(0)) / (Samples - 1) // time between samples
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

  // first centers this stroke given the centroid coords, then
  // converts to a StrokeXY (with a pause given the previous buffer).
  def stroke( cx:Coordinate, cy:Coordinate, p:MouseEventBufferStroke ) = {
    var i = 2*Samples - 1 ; while ( i > 0 ) {
      xy(i) = xy(i) - cy ; i = i - 1
      xy(i) = xy(i) - cx ; i = i - 1 }
    val pauseBefore = if (p == null) 0 else ds(0) - p.ds(p.size-1)
    StrokeXY( pauseBefore, ImmutableValArray(xy), ds(size-1) - ds(0) ) }
}
