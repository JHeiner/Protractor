
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

object Range
{
  val Outer = 5
  val Inner = Doodle.Outer
  val Accept = Color.GREEN.darker
  val Reject = Color.RED.darker
  val Diameter = 2*(Origin-Outer)
  val Ellipse = new Ellipse2D.Double( Outer,Outer, Diameter,Diameter )

  def inside( event:MouseEvent, slop:Int ) = {
	val h = hypot(event)
	( Origin-Inner-slop <= h && h <= Origin-Outer+slop ) }

  def angleFromX( event:MouseEvent ):Double =
	java.lang.Math.atan2( Origin-event.getY, event.getX-Origin )
}

class Range extends Mouser
{
  import Range._

  var angle = NoLimit
  var before = angle
  var last = angle
  val pie = new Arc2D.Double( Ellipse.getBounds2D, -10, 20, Arc2D.PIE )

  def paint( g:Graphics2D ) {
	val c = g.getColor
	val limited = (angle != NoLimit)
	g.setColor( if ( limited ) Reject else Accept ) ; g.fill(Ellipse)
	if ( limited ) { g.setColor(Accept) ; g.fill(pie) }
	g.setColor(c) }

  def wantsStroke( event:MouseEvent ) = inside(event,0)

  def mousePressed( event:MouseEvent ) {
	before = angle
	update( angleFromX(event) ) }

  def mouseDragged( event:MouseEvent ) {
	mouseReleased(event) }

  def mouseReleased( event:MouseEvent ) {
	if ( ! inside(event,15) ) update(before)
	else clip( angleFromX(event) ) }

  def update( v:Double ) {
	last = v
	val a = java.lang.Math.abs(v)
	angle = a
	val d = java.lang.Math.toDegrees(a)
	pie.setAngleStart(-d)
	pie.setAngleExtent(2*d) }

  def clip( a:Double ) {
	if ( ( last < -2 && a > 3 ) || ( last > 2 && a < -3 ) ) update(NoLimit)
	else update(a) }
}
