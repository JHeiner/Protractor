
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

/**
 * Implements a variant of the
 * [[http://www.yangl.org/pdf/protractor-chi2010.pdf Protractor]]^[Li]^
 * gesture recognizer (which is an optimization of the
 * [[http://www.yangl.org/pdf/protractor-chi2010.pdf $1]]^[Wobbrock]^
 * recognizer).

 * All three papers base their sampling on the total distance travelled during
 * a stroke. That total is divided into 32 equal lengths and the stroke is thus
 * resampled uniformly in space. But that throws away the timing information
 * that is available from every UI library I've ever seen.

 * This library records the timestamps of each coordinate event and samples
 * using the total duration of a stroke, ignoring the length completely. If a
 * stroke is performed with a constant velocity, these two approaches produce
 * the same results. But temporal sampling allows the recognizer to distingush
 * between such strokes and strokes which start slow and finish fast, and
 * change velocities in other ways.

 * The papers either perform some scaling (e.g. make strokes fit to a unit
 * size) or calculate the similarity using a metric that ignores scale. The
 * only advantage of this approach would be that you only need one instance of
 * a gesture (say a 'circle') to match any 'circle' of any size. Unfortunately
 * that approach precludes the possibility of assigning different meanings to
 * big and small 'circle's. This library does not throw out scale information.

 * Similarity scores produced by this library are in the unit range. So 0.0
 * means completely different, and 1.0 means identical. There were some
 * surprises in the metrics used in the papers. ACos, for example, has two
 * maxima PI apart, requiring extra work to make sure to avoid the wrong
 * one. TODO: check paper to make sure I'm remembering this correctly... This
 * library is closest to the 3D paper's similarity metric, except it is
 * normalized to the unit range.

 * The papers focus on single-stroke gestures. It is very easy to extend such
 * recognizers to handle multi-stroke gestures, and many such extensions have
 * been published. This library supports multi-stroke gestures, but currently
 * in a not very sophisticated way. TODO: As noted above, the timestamp data is
 * available, but it is unfortunately being ignored when combining the scores
 * for single strokes into scores for multi-stroke gestures. For now, gestures
 * without equal numbers of strokes have similarity 0.0, otherwise it's the
 * product of all the single-stroke scores.

 * == Citations ==
 * 
 * [[http://doi.acm.org/10.1145/1943403.1943468 Sven Kratz and Michael Rohs.
 * 2011. Protractor3D: a closed-form solution to rotation-invariant 3D
 * gestures. In Proceedings of the 16th international conference on Intelligent
 * user interfaces (IUI '11). ACM, New York, NY, USA, 371-374.
 * DOI=10.1145/1943403.1943468]]
 *
 * [[http://doi.acm.org/10.1145/1753326.1753654 Yang Li. 2010. Protractor: a
 * fast and accurate gesture recognizer. In <i>Proceedings of the 28th
 * international conference on Human factors in computing systems</i> (CHI
 * '10). ACM, New York, NY, USA, 2169-2172. DOI=10.1145/1753326.1753654]]
 *
 * [[http://doi.acm.org/10.1145/1294211.1294238 Jacob O. Wobbrock, Andrew D.
 * Wilson, and Yang Li. 2007. Gestures without libraries, toolkits or training:
 * a $1 recognizer for user interface prototypes. In Proceedings of the 20th
 * annual ACM symposium on User interface software and technology (UIST '07).
 * ACM, New York, NY, USA, 159-168. DOI=10.1145/1294211.1294238]]
 */
package object minutiae
{
  /** The number of coordinates to store per stroke. */
  val Samples = 32

  // Storage for coordinates
  type Interleaved = ImmutableValArray[Coordinate]
  val InterleavedEmpty = ImmutableValArray.empty[Coordinate]
}

// historical note: I moved the following into their own files when I started
// using ScalaIDE because it seemed to have occasional difficulty finding the
// source code for something (e.g. with F3, or clicking on stack traces).
// ScalaIDE is getting smarter, and the following aren't relevant in debugging
// anyway, so I moved them back here. I liked having the original organization
// (4 files, no deep folders) because this project is so small. I wish I could
// go back to that, but for now there's this uneasy balance between the way
// Java/Eclipse wants things to be and the way Scala/SBT lets things be.

package minutiae
{
  trait Stroke extends Immutable
  {
    /** In a Gesture, for strokes after the 1st one, the time between the end
      * of the previous stroke and this one. */
    def pauseBefore:Time
    require( pauseBefore >= 0, "negative pause" )

    /** The total time taken to make this stroke. */
    def duration:Time
    require( duration >= 0, "negative duration" )

    /** Only 2D gestures are supported right now. Eventually 3D. */
    def dimension:Int
    require( dimension == 2, "unsupported dimension: "+dimension )

    /** The coordinates, interleaved within a single array. */
    def interleaved:Interleaved
    require( interleaved.size % dimension == 0, "wrong coordinate count" )

    /** Returns the square of the magnitude (== this dot this). */
    def mm:Double
  }

  trait SeqStroke extends IndexedSeq[Stroke]
  {
    /** @see [[protractor.Gesture.optimal]] */
    def optimal( fixed:SeqStroke ):Rotation

    /** @see [[protractor.Gesture.compareAt]] */
    def compareAt( r:Rotation, fixed:SeqStroke ):Similarity
  }

  object SeqStroke
  {
    implicit def apply( strokes:Seq[Stroke] ):SeqStroke = {
      val array = strokes.map(_.asInstanceOf[StrokeXY]).toArray
      val centroid = (for ( s <- array ) yield s.sumXY).transpose.map{_.sum}
      require( centroid.forall{isNearZero(_)}, "not centered" )
      new SeqStrokeXY(array) }

    def isNearZero( v:Double ):Boolean =
      -1e-10 <= v && v <= 1e-10
  }

  /** The Rotation representing "none". */
  object ZeroRotation extends Rotation(0)
  {
    override def limited( to:Double ) = this
    override def apply( strokes:SeqStroke ):SeqStroke = strokes
  }
}
