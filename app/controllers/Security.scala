package controllers

import play.api._
import play.api.mvc._

import play.api.cache._
import play.api.libs.json._

trait Security { self: Controller =>
  implicit val app: play.api.Application = play.api.Play.current

  val AuthTokenHeader = "X-XSRF-TOKEN"
  val AuthTokenCookieKey = "XSRF-TOKEN"
  val AuthTokenUrlKey = "auth"

  def HasToken[A](p: BodyParser[A] = parse.anyContent)(f: String => String => Request[A] => Result): Action[A] =
    Action(p) { implicit request =>
      val maybeToken = request.headers.get(AuthTokenHeader).orElse(request.getQueryString(AuthTokenUrlKey))
      maybeToken flatMap { token =>
        Cache.getAs[String](token) map { jobNo =>
          f(token)(jobNo)(request)
        }
      } getOrElse Unauthorized(Json.obj("err" -> "No Token"))
    }

}