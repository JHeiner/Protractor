
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

class MouseEventBufferStrokes
{
  var firstPress:Long = 0
  var lastDelta:Int = 0
  val strokes = new scala.collection.mutable.ArrayBuffer[MouseEventBufferStroke]

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