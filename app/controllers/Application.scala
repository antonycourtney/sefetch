package controllers

import java.io._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import tc.FetchArgs
import tc.SETransform

object Application extends Controller {
  val fa = new FetchArgs()

  // val previewFile = new File( "/Users/antony/tmp/tcse-2013-04-27.d/out/out.xhtml" )
  // val wpFile = new File( "/Users/antony/tmp/tcse-2013-04-27.d/out/wp.xhtml" )

  val reportForm = Form(
    "endDate" -> nonEmptyText
  )
  
  def index = Action {
    Ok(views.html.index( fa ))
  }
 
  def previewCurrent = Action {
    val res = tc.SETransform.fetch( fa )
    Ok.sendFile( new File( res.previewFile ), inline = true ).as("text/html")
  }

  def downloadWPCurrent = Action {
    val res = tc.SETransform.fetch( fa )
    Ok.sendFile( new File( res.wpFile ) )
  }

  def viewWPCurrent = Action {
    val res = tc.SETransform.fetch( fa )
    Ok.sendFile( new File( res.wpFile ), inline = true ).as("text/plain")
  }  

  /*
  def previewCurrent = Action { implicit request =>
  	reportForm.bindFromRequest.fold(
  		errors => BadRequest(views.html.index(errors)),
  		endDate => {
  			Redirect(routes.Application.reportForm)
  		}
	)
  }
 */
}