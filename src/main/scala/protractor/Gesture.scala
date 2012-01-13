
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

class Gesture( val strokes:minutiae.StrokeSeq, val limit:Angle )
extends Immutable
{
  require( limit >= 0, "limit is negative" )
  def limit( limit:Angle ) = new Gesture( strokes, limit )
  def rotate( r:Rotation ) = new Gesture( r(strokes), limit )

  override def toString = strokes.mkString("Gesture( ",", "," )") +
	(if ( limit >= java.lang.Math.PI ) "" else ".limit("+limit+")")

  def optimal( fixed:Gesture ):Rotation =
    this.strokes.optimal(fixed.strokes)

  def limited( r:Rotation, fixed:Gesture ):Rotation =
    r.limited( this.limit min fixed.limit )

  def compareAt( r:Rotation, fixed:Gesture ):Similarity =
    this.strokes.compareAt(r,fixed.strokes)

  def similarity( fixed:Gesture ):Similarity =
    compareAt( limited( optimal(fixed), fixed ), fixed )
}
