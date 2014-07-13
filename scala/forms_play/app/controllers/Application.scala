package controllers

import play.api._
import play.api.mvc._
import deductions.runtime.html.TableView

object Application extends Controller with TableView {
  def index = {
    Action {
      (
          Ok( views.html.index(global.Global.form) )
      )
    }
  }
}