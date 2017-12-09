package deductions.runtime.js

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom
import org.scalajs.dom.window
import org.scalajs.dom.document
import org.scalajs.dom.raw.Geolocation
import org.scalajs.dom.raw.Position
import org.scalajs.dom.raw.PositionError
import org.scalajs.dom.raw.Node
import org.scalajs.dom.html.Input
// for for loop with NodeList:
import org.scalajs.dom.ext._

@JSExportTopLevel("GPS2")
object GPS2 extends GPS {

  /**
   * Fill Geo Coordinates in form
   */
  @JSExport
  def listenToSubmitEventFillGeoCoordinates(): Unit = {
    if( GeoCoordinatesFields.pageNeedsGeoCoordinates() . needsUpdate )
      window.addEventListener("load", (e: dom.Event) => fillGeoCoordinates)
  }

  /**
   * fill longitude & latitude from HTML5 GPS API into relevant geo:long & geo:lat triples
   */
  private def fillGeoCoordinates {
	  val window = dom.document.defaultView
    val nav = window.navigator
    val geo: Geolocation = nav.geolocation

   /* obtain longitude & latitude from HTML5 GPS API,
    * and then call fillCoords();
    * cf http://stackoverflow.com/questions/40483880/geolocation-in-scala-js */
    geo.watchPosition( fillCoords _, onError _)
  }

  import scala.scalajs.js.Dynamic.global

  def onError(p: PositionError) =
    global.console.log("geoLocation: Error: " + p)
    // println(s"geoLocation: Error $p")

}