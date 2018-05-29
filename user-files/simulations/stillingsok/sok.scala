package stillingsok

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._

class Stillingsok extends Simulation {

  val httpProtocol = http
    .baseURL("https://pam-stillingsok.nais.oera.no")
    .inferHtmlResources(BlackList(""".*\.css""", """.*\.js""", """.*\.ico"""), WhiteList())
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.9")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36 OPR/52.0.2871.64")

  val headers_0 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Upgrade-Insecure-Requests" -> "1")

  val headers_1 = Map(
    "content-type" -> "application/json",
    "origin" -> "https://pam-stillingsok.nais.oera.no")

  val uri1 = "https://pam-stillingsok.nais.oera.no:443"

  object Search {

    val searchInput = csv("yrker.csv").random

    val search = feed(searchInput)
      .exec(http("request_get_front_page")
        .get("/")
        .headers(headers_0)
        .resources(http("request_initial_search")
          .post("/pam-stillingsok/search-api/ad/_search")
          .headers(headers_1)
          .body(ElFileBody("stillingsok_request_initial_search.txt"))))
      .pause(5)
      .exec(http("request_typeahead")
        .post("/pam-stillingsok/search-api/ad/_search")
        .headers(headers_1)
        .body(ElFileBody("stillingsok_request_typeahead.txt")))
      .pause(1)
      .exec(http("request_search")
        .post("/pam-stillingsok/search-api/ad/_search")
        .headers(headers_1)
        .body(ElFileBody("stillingsok_request_search.txt"))
        .check(jsonPath("$.timed_out").is("false")))
//        .check(jsonPath("$").saveAs("response"))
//      .exec(session => { // Print respons
//        println("request_search:")
//        println(session("response").as[String])
//        session
//      })
  }

  val search = scenario("Search").exec(Search.search)

  setUp(
    //search.inject(atOnceUsers(500))
    search.inject(rampUsers(1000) over (10 seconds))
  ).protocols(httpProtocol)
}