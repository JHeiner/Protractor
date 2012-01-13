
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

object Doodle
{
  val Outer = 12
  val Gradient = interpolate( 6, Color.BLACK, Color.LIGHT_GRAY )
  val Diameter = 2*(Origin-Outer)
  val Ellipse = new Ellipse2D.Double( Outer,Outer, Diameter,Diameter )

  def inside( event:MouseEvent, slop:Int ) =
    ( hypot(event) <= Origin-Outer+slop )

    def decorate( p:java.awt.geom.Path2D, g:Gesture ) {
      p.reset
      if ( null != g )
	g.strokes foreach { s => decorateXY( p, s.interleaved ) } }

  def decorateXY( p:java.awt.geom.Path2D, xy:IndexedSeq[Coordinate] ) {
    val x = xy(0) + Origin
    val y = xy(1) + Origin
    p.moveTo(x-2,y-2) ; p.lineTo(x+2,y-2) ; p.lineTo(x+2,y+2)
    p.lineTo(x-2,y+2) ; p.lineTo(x-2,y-2) ; p.moveTo(x,y)
    for ( i <- 2 until xy.size by 2 ) {
      val x = xy(i+0) + Origin
      val y = xy(i+1) + Origin
      p.lineTo(x+1,y) ; p.lineTo(x-1,y) ; p.lineTo(x,y)
      p.lineTo(x,y+1) ; p.lineTo(x,y-1) ; p.lineTo(x,y) } }

  def interpolate( n:Int, one:Color, two:Color ):Array[Color] = {
    require( n > 2, "interpolate asked for fewer than 3 points" )
    val o = Color.RGBtoHSB(one.getRed,one.getGreen,one.getBlue,null)
    val d = Color.RGBtoHSB(two.getRed,two.getGreen,two.getBlue,null)
    for ( i <- 0 until 3 ) d(i) = d(i) - o(i)
    val a = Array.ofDim[Color](n)
    a(0) = one ; a(n-1) = two
    for ( i <- 1 until n-1 ) a(i) =
      Color.getHSBColor(o(0)+i*d(0)/n,o(1)+i*d(1)/n,o(2)+i*d(2)/n)
    a }
}

class Doodle extends Mouser
{
  import Doodle._

  val input = new MouseEventBuffer
  //val interactive = new Path2D.Double
  val processed = new Path2D.Double
  var rotated = new Path2D.Double
  var gesture:Gesture = null
  val timer = new Timer( 100, null )
  timer.setInitialDelay( 400 )
  var count = -1

  def paint( g:Graphics2D ) {
    val c = g.getColor
    g.setColor(Color.WHITE)
    g.fill(Ellipse)
    if ( count != 0 ) {
      g.setColor( Gradient( if (count<0) 0 else (Gradient.length-count) ) )
      input.drawPolyline( g ) }
    if ( count >= 0 ) {
      if ( count == 0 ) {
	g.setColor( Color.LIGHT_GRAY )
	g.draw( rotated ) }
      g.setColor( Gradient( count ) )
      g.draw( processed ) }
    g.setColor(c) }

  def wantsStroke( event:MouseEvent ) = inside(event,0)

  override def mousePressed( event:MouseEvent ) {
    if ( timer.isRunning ) timer.stop else input.clear//interactive.reset
    input.pressed( event )//interactive.moveTo(event.getX,event.getY)
    count = -1 }

  override def mouseDragged( event:MouseEvent ) {
    input.dragged( event ) }
  //interactive.lineTo(event.getX,event.getY) }

  override def mouseReleased( event:MouseEvent ) {
    input.released( event )//interactive.lineTo(event.getX,event.getY)
    gesture = input.gesture
    Doodle.decorate(processed,gesture)
    timer.restart }

  override val getTimer = Some(timer)
  override def timerFired() {
    if ( count < 0 ) count = Gradient.length - 1
    else if ( count > 0 ) count = count - 1
    if ( count == 0 ) timer.stop }
}
