package com.example

import org.slf4j.LoggerFactory

import unfiltered.request._
import unfiltered.response._
import unfiltered.Cookie

import dispatch.classic._

import dispatch.classic.oauth._
import dispatch.classic.oauth.OAuth._

import dispatch.liftjson.Js._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.util.control.NonFatal

class App(consumer: Consumer) extends Templates with unfiltered.filter.Plan {
  import QParams._

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  private val svc = :/("localhost", 8080)
  private val tmap = scala.collection.mutable.Map.empty[String, ClientToken]

  object AuthorizedToken {
    def unapply[T](r: HttpRequest[T]) = r match {
      case Cookies(cookies) => cookies("token") match {
        case Some(c: Cookie) => Some(AccessToken.fromCookieString(c.value))
        case _ => None
      }
    }
  }

  def intent = {
    // if we have an access token on hand, make an api call
    // if not, render the current list of tokens
    case GET(Path("/") & AuthorizedToken(at)) =>
      try {
        Http(svc / "api" / "user" <@(consumer, at.asDispatchToken, at.verifier) ># { js  =>
          val response = pretty(render(js))
          logger.info("made successful api call " + response)
          apiCall(response)
        })
      } catch { case NonFatal(e) =>
        val msg = "there was an error making an api request: " + e.getMessage
        logger.warn(msg)
        apiCall(msg)
      }

    // show a list of tokens, if any, and a way to connect
    case GET(Path("/")) => tokenList(tmap.values)

    // kickoff for oauth dance party
    case GET(Path("/connect")) =>
      val token = Http(svc.POST / "oauth" / "request_token" <@(consumer, "http://localhost:8081/authorized") as_token)
      logger.info("fetched token unauthorized request token " + token.value)
      tmap += (token.value -> RequestToken(token.value, token.secret))
      Redirect("http://localhost:8080/oauth/authorize?oauth_token=%s" format(token.value))

   // clear the current authorized token
   case GET(Path("/disconnect")) =>
     SetCookies(Cookie("token", "")) ~> Redirect("/")

    // post user authorization callback uri
    case GET(Path("/authorized") & Params(params)) =>
      val expected = for {
        verifier <- lookup("oauth_verifier") is
          required("verifier is required") is nonempty("verifier can not be blank")
        token <- lookup("oauth_token") is
          required("token is required") is nonempty("token can not be blank")
      } yield {
        logger.info("recieved authorization for token %s from verifier %s" format(token.get, verifier.get))
        val access_token = Http(svc.POST / "oauth" /  "access_token" <@(consumer, tmap(token.get).asDispatchToken, verifier.get) as_token)
        logger.info("fetched access token %s" format access_token.value)
        tmap -= token.get
        SetCookies(Cookie("token", AccessToken(access_token.value, access_token.secret, verifier.get).toCookieString)) ~> Redirect("/")
      }

      expected(params) orFail { fails =>
        BadRequest ~> ResponseString(fails.map { _.error } mkString(". "))
      }

    case GET(Path(Seg("tokens" :: "delete" ::  key :: Nil))) =>
      tmap -= key
      Redirect("/")
  }
}
