
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

package protractor.gui

import protractor._
import java.awt.{Color,Dimension,Graphics2D}
import java.awt.event.{ActionEvent,ActionListener,MouseEvent}
import java.awt.geom.{Arc2D,Ellipse2D,Path2D}
import scala.collection.mutable.{ArrayBuffer,ListBuffer}
import scala.annotation.tailrec
import Common._
import javax.swing.Timer
import java.awt.event.{MouseListener,MouseMotionListener}
import javax.swing.event.MouseInputAdapter
import javax.swing._

class TemplateComponent( unknown:UnknownComponent ) extends RangeDoodle
{
  var score:String = null

  override def paintComponent2D( g:Graphics2D ) {
    super.paintComponent2D(g)
    if ( null != score )
      g.drawString(score,10,2*Origin-5) }

  def updated() {
    val d = doodle
    val t = d.gesture
    if ( null != t ) {
      val u = unknown.doodle.gesture
      if ( null != u ) {
	val o = u.optimal(t)
	val l = u.limited(o,t)
	val s = u.compareAt(l,t)
	Doodle.decorate(d.rotated,u.rotate(l))
	score = scoreFormat.format(s) }
      repaint() } }
}
