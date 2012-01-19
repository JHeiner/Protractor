
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

/** The public API for this gesture recognition library. The implementation
  * details (including documentation of the algorithm) are "hidden" in the
  * [[protractor.minutiae]] package.
  *
  * The fundamental units that the recognition algorithm works with are
  * [[protractor.Stroke]] instances, which can be the movement of the mouse
  * starting at a `mousePressed` and ending with a `mouseReleased`, or the
  * movement of a finger while it is in contact with a touch-sensitive device.
  *
  * A sequence of one or more consecutive [[protractor.Stroke]] instances, with
  * [[protractor.Pause]] intervals between them, is wrapped in a
  * [[protractor.Gesture]] instance, which provides the methods to compare it
  * to other [[protractor.Gesture]] instances.
  *
  * [[protractor.MouseEventBuffer]] can be used to collect incoming
  * [[java.awt.event.MouseEvent]] data and convert it to a
  * [[protractor.Gesture]].
  *
  * @define uniform The points are expected to be sampled at a uniform rate (so
  * the time elapsed between each pair of consecutive points is a constant).
  */
package object protractor
{
  /** Holds motion data. Instances are immutable. The details are not
    * relevant to most applications, but can be found at
    * [[protractor.minutiae.Stroke]].
    */
  type Stroke = minutiae.Stroke

  /** Time values are double precision, but the units are unspecified.
    * The application must be consistent if it wants meaningful results.
    */
  type Time = Double

  /** Coordinate values are double precision, but the units are unspecified.
    * The application must be consistent if it wants meaningful results.
    */
  type Coordinate = Double

  /** Angle values are double precision and measured in radians.
    */
  type Angle = Double

  /** Holds rotation data (an angle and an axis). Instances are immutable.
    * The details are not relevant to most applications, but can be found at
    * [[protractor.minutiae.Rotation]].
    */
  type Rotation = minutiae.Rotation

  /** A value for use in methods of [[protractor.Gesture]] to indicate that
    * the angle of rotation should not be limited.
    */
  val NoLimit:Angle = 4 // anything PI or larger works

  /** Results of a comparison between two [[protractor.Gesture]] instances.
    * Values are double precision in the unit range, where 1.0 indicates the
    * two instances are identical, and 0.0 indicates no similarity at all.
    */
  type Similarity = Double

  /** Constructs a [[protractor.Stroke]] of two-dimensional points. $uniform
    * @param duration total time elapsed between the first and last coords -
    * should be non-zero unless there is only a single point.
    * @param xyInterleaved the coords of each two-dimensional point. There must
    * be an even number of coords, and at least two of them. The `x` coord of
    * the first point comes first, followed by the `y` coord of the first
    * point, then the `x` coord of the second point, etc.
    */
  def StrokeXY( duration:Time, xyInterleaved:Coordinate * ):Stroke =
	minutiae.StrokeXY( 0, xyInterleaved, duration )

  /** Placed before a [[protractor.Stroke]] constructor in the call to the
    * [[protractor.Gesture]] constructor to indicate the length of pauses
    * between strokes.
    */
  def Pause( pauseBefore:Time ) = new {
	def StrokeXY( duration:Time, xyInterleaved:Coordinate * ):Stroke =
	  minutiae.StrokeXY( pauseBefore, xyInterleaved, duration ) }

  /** Constructs a [[protractor.Gesture]] from a Seq[ [[protractor.Stroke]] ].
    * The first stroke shouldn't have a [[protractor.Pause]], but all the
    * subsequent ones should (and should be non-zero). Calling the `toString`
    * method on a [[protractor.Gesture]] instance returns Scala source code
    * that calls this function to re-construct the [[protractor.Gesture]].
    */
  def Gesture( strokes:Stroke * ):Gesture = new Gesture( strokes, NoLimit )
}
