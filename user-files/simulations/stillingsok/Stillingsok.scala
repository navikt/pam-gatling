package stillingsok

import scala.concurrent.duration._
import scala.util.Random

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class Stillingsok extends Simulation {

  val prod = "https://stillingsok.nav.no/"
  val test = "https://pam-stillingsok.nais.oera-q.local"

  val env = test // Miljøet som testen skal kjøres på

  val httpProtocol = http
    .baseURL(env)
    .inferHtmlResources(BlackList(""".*\.css""", """.*\.js""", """.*\.ico"""), WhiteList())
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.9")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36 OPR/52.0.2871.64")

  val uri1 = env + ":443"

  val searchApiSearch = "/api/search"
  var searchApiSuggestions = "/api/suggestions"

  // Headers
  val headers_0 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Upgrade-Insecure-Requests" -> "1")
  val headers_1 = Map(
    "content-type" -> "application/json",
    "origin" -> env)
  val headers_4 = Map("origin" -> env)

  // Input data
  val searchInput = csv("yrker.csv").random
  val searchInputPlace = csv("fylker.csv").random

  // Laster inn forsiden med et søkeresultat på alt
  object FrontPage {

    val loadFrontPage = exec(http("request_get_front_page")
      .get("/")
      .headers(headers_0)
      .resources(http("request_initial_search")
        .post(searchApiSearch)
        .headers(headers_1)
        .body(ElFileBody("stillingsok_request_initial_search.txt"))))
      .pause(5)
  }

  // Gjør et søk inkludert typeahead
  object Search {

    val search = feed(searchInput)
      .feed(searchInputPlace)
      .exec(http("request_typeahead")
        .post(searchApiSuggestions)
        .headers(headers_1)
        .body(ElFileBody("stillingsok_request_typeahead.txt")))
      .pause(1)
      .exec(_.set("FROM", 0))
      .exec(http("request_search")
        .post(searchApiSearch)
        .headers(headers_1)
        .body(ElFileBody("stillingsok_request_search.txt"))
        .check(jsonPath("$.timed_out").is("false"))
        .check(jsonPath("$.hits.total").saveAs("treff"))
        .check(jsonPath("$.hits.hits[0]._id").saveAs("id")))
  }

  // Navigerer til den siste siden for et søkeresultat
  object LastPage {

    val lastPage = exec(session => session.set("FROM", session("treff").as[String].toInt - 1))
      .exec(http("request_search_last_page")
        .post(searchApiSearch)
        .headers(headers_1)
        .body(ElFileBody("stillingsok_request_search.txt"))
        .check(jsonPath("$.timed_out").is("false")))
  }

  // Åpner den øverste annonsen for et søkeresultat
  object OpenAnnonse {

    val openAnnonse = exec(http("request_open_annonse")
      .get(s"/api/stilling/" + _("id").as[String])
      .headers(headers_4))
  }

  val search = scenario("Search").exec(FrontPage.loadFrontPage, Search.search)
  val searchLastPage = scenario("SearchLastPage").exec(FrontPage.loadFrontPage, Search.search, LastPage.lastPage)
  val searchOpenAnnonse = scenario("SearchOpenAnnonse").exec(FrontPage.loadFrontPage, Search.search, OpenAnnonse.openAnnonse)

  setUp(
    search.inject(rampUsers(250) over (10 seconds)),
    searchLastPage.inject(rampUsers(250) over (10 seconds)),
    searchOpenAnnonse.inject(rampUsers(250) over (10 seconds))
  ).protocols(httpProtocol)
}
