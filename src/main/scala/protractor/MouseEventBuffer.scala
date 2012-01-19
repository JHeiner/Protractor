
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

/** Collects AWT events, provides [[protractor.MouseEventBuffer.gesture]] to
  * convert the mouse motion data into a [[protractor.Gesture]] instance.
  * The [[protractor.gui.Doodle]] demonstrates its use.
  *
  * ''Warning'' : Instances of this class are '''not''' thread safe. But In
  * a typical AWT application all events are delivered by a single thread,
  * so synchronization not an issue.
  */
class MouseEventBuffer extends minutiae.MouseEventBufferStrokes
{
//TODO: change from inheriting to a field member to expose the right API

  /** Discard all collected stroke data.
    */
  def clear() { strokes.clear() }

  import java.awt.event.MouseEvent

  /** Add a `mousePressed` event to the stroke data.
    */
  def pressed( e:MouseEvent ) {
    startNewStroke( e.getWhen ) ; add( e.getX, e.getY, e.getWhen ) }

  /** Add a `mouseDragged` event to the stroke data.
    */
  def dragged( e:MouseEvent ) { add( e.getX, e.getY, e.getWhen ) }

  /** Add a `mouseReleased` event to the stroke data.
    */
  def released( e:MouseEvent ) { add( e.getX, e.getY, e.getWhen ) }

  /** Calls [[java.awt.Graphics.drawPolyline]] with the collected stroke data.
    */
  def drawPolyline( g:java.awt.Graphics ) {
    strokes foreach { _.drawPolyline(g) } }
}

