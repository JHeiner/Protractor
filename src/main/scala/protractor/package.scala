
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

/**
 * Implements a variant of the algorithm described in:
 *
 * Yang Li. 2010. Protractor: a fast and accurate gesture recognizer. In <i>Proceedings of the 28th international conference on Human factors in computing systems</i> (CHI '10). ACM, New York, NY, USA, 2169-2172. DOI=10.1145/1753326.1753654 http://doi.acm.org/10.1145/1753326.1753654
 *
 * http://www.yangl.org/pdf/protractor-chi2010.pdf
 *
 * Which is an optimization of the algorithm described in:
 *
 * Jacob O. Wobbrock, Andrew D. Wilson, and Yang Li. 2007. Gestures without libraries, toolkits or training: a $1 recognizer for user interface prototypes. In Proceedings of the 20th annual ACM symposium on User interface software and technology (UIST '07). ACM, New York, NY, USA, 159-168. DOI=10.1145/1294211.1294238 http://doi.acm.org/10.1145/1294211.1294238 
 *
 * http://faculty.washington.edu/wobbrock/pubs/uist-07.1.pdf
 */
package object protractor
{
  type Stroke = minutiae.Stroke // extends Immutable

  type Time = Double // in milliseconds
  type Coordinate = Double // in pixels
  def StrokeXY( duration:Time, xyInterleaved:Coordinate * ):Stroke =
	minutiae.StrokeXY( 0, xyInterleaved, duration )

  def Pause( pauseBefore:Time ) = new {
	def StrokeXY( duration:Time, xyInterleaved:Coordinate * ):Stroke =
	  minutiae.StrokeXY( pauseBefore, xyInterleaved, duration ) }

  type Angle = Double // in radians, of course
  val NoLimit:Angle = 4 // anything PI or larger

  def Gesture( strokes:Stroke * ):Gesture = new Gesture( strokes, NoLimit )
  def Gesture( input:MouseEventBuffer ):Gesture = input.gesture

  type Rotation = minutiae.Rotation // Angle + axis, extends Immutable
  type Similarity = Double // 1.0 is identical .. 0.0 is least similar
}
