
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

/** Holds the information about a gesture - possibly one just captured from
  * the user's manipulation of a hardware device (mouse, touchscreen, etc.),
  * or maybe one that has been stored in the application with an action to
  * take when the user makes that gesture. The primary purpose of this
  * library is to provide a way to measure how similar two `Gesture`s are to
  * each other - the [[protractor.Gesture.similarity]] method does that.
  *
  * Note that this constructor is not really part of the public API - most
  * applications will use [[protractor.MouseEventBuffer.gesture]] or the
  * [[protractor.Gesture]] function in the [[protractor]] package object.
  *
  * @param strokes the "massaged" stroke data
  * @param limit the maximum angle this gesture is allowed to be rotated
  * before calculating the distances between stroke points.
  */
class Gesture( val strokes:minutiae.SeqStroke, val limit:Angle )
extends Immutable
{
  require( limit >= 0, "limit is negative" )

  /** Returns a new `Gesture` with the same strokes, but a different limit. */
  def limit( limit:Angle ) = new Gesture( strokes, limit )

  /** Returns a new `Gesture` with the same limit, rotating the strokes. */
  def rotate( r:Rotation ) = new Gesture( r(strokes), limit )

  /** Returns Scala source code that reconstructs this `Gesture`. */
  override def toString = strokes.mkString("Gesture( ",", "," )") +
	(if ( limit >= java.lang.Math.PI ) "" else ".limit("+limit+")")

  /** Returns the [[protractor.Rotation]] which, when applied to this
    * `Gesture` minimizes the distance between the rotated strokes and the
    * `fixed` `Gesture`'s strokes.
    */
  def optimal( fixed:Gesture ):Rotation =
    this.strokes.optimal(fixed.strokes)

  /** Returns a [[protractor.Rotation]] with the same axis as the `r`
    * parameter, but with an angle that exceeds neither this
    * `Gesture`'s nor the `fixed` parameter's `limit` in magnitude.
    */
  def limited( r:Rotation, fixed:Gesture ):Rotation =
    r.limited( this.limit min fixed.limit )

  /** Rotates this `Gesture`'s strokes by the given [[protractor.Rotation]],
    * then compares those to the `fixed` parameter's strokes.
    */
  def compareAt( r:Rotation, fixed:Gesture ):Similarity =
    this.strokes.compareAt(r,fixed.strokes)

  /** Returns `compareAt( limited( optimal(fixed), fixed ), fixed )`
    */
  def similarity( fixed:Gesture ):Similarity =
    compareAt( limited( optimal(fixed), fixed ), fixed )
}
