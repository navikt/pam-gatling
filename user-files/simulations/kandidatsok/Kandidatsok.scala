package kandidatsok

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class Kandidatsok extends Simulation {

  // Environment
  val prod = "https://tjenester.nav.no"
  val t6 = "https://tjenester-t6.nav.no"
  val q6 = "https://tjenester-q6.nav.no"
  val q0 = "https://tjenester-q0.nav.no"

  val env = prod

  // Protocol
  val httpProtocol = http
    .baseURL(env)
    .inferHtmlResources(BlackList(""".*\.css""", """.*\.js""", """.*\.ico"""), WhiteList())
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.9,nb;q=0.8")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36")

  // Headers
  val headers_0 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
    "Upgrade-Insecure-Requests" -> "1")

  val uri1 = env + ":443/pam-kandidatsok"

  // Input data
  val stillingInput = csv("kandidatsokStilling.csv").random
  val utdanningInput = csv("kandidatsokUtdanning.csv").random
  val utdanningsNivaInput = csv("kandidatsokUtdanningsNiva.csv").random
  val arbeidserfaringArInput = csv("kandidatsokArbeidserfaringAr.csv").random
  val kompetanseInput = csv("kandidatsokKompetanse.csv").random
  val geografiInput = csv("kandidatsokGeografi.csv").random
  val sprakInput = csv("kandidatsokSprak.csv").random

  // Cookie value - Denne må genereres manuelt i en browser før en kjøring, og limes inn her
  val cookieValue = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ijc2dDY5UVBteVBDNVFNVloybkVxenFaNHBEVnZ4dWVlS0hkUEtXQ3QwOWMifQ.eyJleHAiOjE1MzQ1MjAwNzcsIm5iZiI6MTUzNDUxNjQ3NywidmVyIjoiMS4wIiwiaXNzIjoiaHR0cHM6Ly9sb2dpbi5taWNyb3NvZnRvbmxpbmUuY29tLzhiN2RmYzhiLWI1MmUtNDc0MS1iZGU0LWQ4M2VhMzY2Zjk0Zi92Mi4wLyIsInN1YiI6IjExMDM4ODMzNzg0IiwiYXVkIjoiNDUxMDRkNmEtZjViYy00ZThjLWIzNTItNGJiZmM5MzgxZjI1IiwiYWNyIjoiTGV2ZWw0Iiwibm9uY2UiOiJwME5lbHpQQUdkblVTVU82eTVvLV9pR2ZzTjdVSFJBVmtXOUU3STRMZnI0IiwiaWF0IjoxNTM0NTE2NDc3LCJhdXRoX3RpbWUiOjE1MzQ1MTY0NzcsImp0aSI6Imo1eUUtbTJqYUYteE12ZlpkbzcyQ2hVSGRJNEpoVzNhbTFZWjZQNG1xNFE9IiwiYXRfaGFzaCI6ImZnTXZ4M2h4TkNWQ0pNa0ZVb3JyMEEifQ.o8ZK7OXmnJ5uHvLAhgrSfc85d5OYkHB7L0wHafrY0grjN-EfJX82l_ccsaW0Syldj5_94gJcor_L2ZQCAo3xFDbQDMJlOriQdx89qMRgM3PSVuuaLGkInqWknMzPkOxM9Bkgn_3btoW_CiknuhLr3UZLJ6vnAddaKrFC-OtSpMldXzetai3FbEMdquJYrMtAezQ3S5Dv8QNOVIuBP27IS7vvKFLZFt8LN8xfhU1u8etXd__4jeVJnLj3sXqpjq9d7H4ifqiH_xgV6xKXlBdzN78suESi3Vv5pNpdKdG9wyUMGf_sRPe3AYJh_rB2H3JdxHZ2ddzT9Io72RQQ5lGylw"

  val globalPause = 1 // Global pause mellom requests, i sekunder

  object Login {
     val login = exec(addCookie(Cookie("selvbetjening-idtoken", cookieValue)
      .withDomain(".nav.no")))
  }

  object LoadFirstPage {
    val loadFirstPage = exec(http("Last forside")
      .get("/pam-kandidatsok")
      .headers(headers_0)
      .resources(http("Feature toggle")
        .get("/pam-kandidatsok/rest/kandidatsok/toggles?feature=vis-manglende-arbeidserfaring-boks,janzz-enabled,skjul-yrke,skjul-kompetanse,skjul-utdanning,skjul-arbeidserfaring,skjul-spraak,skjul-sted"),
        http("Søk uten kriterier")
          .get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=&utdanningsniva=&sprak=")))
  }

  object Typeahead {
    def value(category: String) : String = {
      category match {
        case "stilling" => return "sti=${STILLINGTYPEAHEAD}"
        case "arbeidserfaring" => return "yrke=${STILLINGTYPEAHEAD}"
        case "utdanning" => return "utd=${UTDANNINGTYPEAHEAD}"
        case "kompetanse" => return "komp=${KOMPETANSETYPEAHEAD}"
        case "geografi" => return "geo=${GEOGRAFITYPEAHEAD}"
        case "språk" => return "sprak=${SPRAKTYPEAHEAD}"
        case default => return null
      }
    }

    def typeahead(category: String) = exec(http("Typeahead " + category)
      .get("/pam-kandidatsok/rest/kandidatsok/typeahead?" + value(category)))
  }

  object Search {
    def searchRequest(criterias: String) : String = {
      val request = ("/pam-kandidatsok/rest/kandidatsok/sok?"
        + "stillinger=" + (if (criterias.contains("stilling")) "${STILLING}" else "") + "&"
        + "arbeidserfaringer=" + (if (criterias.contains("arbeidserfaring")) "${STILLING}" else "") + "&"
        + "utdanninger=" + (if (criterias.contains("utdanning")) "${UTDANNING}" else "") + "&"
        + "kompetanser=" + (if (criterias.contains("kompetanse")) "${KOMPETANSE}" else "") + "&"
        + "geografiList=" + (if (criterias.contains("geografi")) "${GEOGRAFI}" else "") + "&"
        + "totalErfaring=" + (if (criterias.contains("år")) "${AR}" else "") + "&"
        + "utdanningsniva=" + (if (criterias.contains("utdanningsnivå")) "${UTDANNINGSNIVA}" else "") + "&"
        + "sprak=" + (if (criterias.contains("språk")) "${SPRAK}" else ""))

        return request
    }

    def search(criterias: String) = {
      exec(http("Søk - " + criterias)
        .get(searchRequest(criterias)))
    }
  }

  // Scenarioer

  val firstPage = scenario("First page")
    .exec(Login.login, LoadFirstPage.loadFirstPage)

  val typeahead = scenario("Typeahead")
    .feed(stillingInput)
    .feed(utdanningInput)
    .feed(kompetanseInput)
    .feed(geografiInput)
    .feed(sprakInput)
    .exec(Login.login, Typeahead.typeahead("stilling"), Typeahead.typeahead("arbeidserfaring"), Typeahead.typeahead("utdanning"), Typeahead.typeahead("kompetanse"), Typeahead.typeahead("geografi"), Typeahead.typeahead("språk"))

  val typeaheadStilling = scenario("Typeahead - stilling")
    .feed(stillingInput)
    .exec(Login.login, Typeahead.typeahead("stilling"))

  val typeaheadArbeidserfaring = scenario("Typeahead - arbeidserfaring")
    .feed(stillingInput)
    .exec(Login.login, Typeahead.typeahead("arbeidserfaring"))

  val typeaheadUtdanning = scenario("Typeahead - utdanning")
    .feed(utdanningInput)
    .exec(Login.login, Typeahead.typeahead("utdanning"))

  val typeaheadGeografi = scenario("Typeahead - geografi")
    .feed(geografiInput)
    .exec(Login.login, Typeahead.typeahead("geografi"))

  val typeaheadKompetanse = scenario("Typeahead - kompetanse")
    .feed(kompetanseInput)
    .exec(Login.login, Typeahead.typeahead("kompetanse"))

  val typeaheadSprak = scenario("Typeahead - språk")
    .feed(sprakInput)
    .exec(Login.login, Typeahead.typeahead("språk"))

  val search = scenario("Search")
    .feed(stillingInput)
    .feed(utdanningInput)
    .feed(utdanningsNivaInput)
    .feed(arbeidserfaringArInput)
    .feed(kompetanseInput)
    .feed(geografiInput)
    .feed(sprakInput)
    .exec(Login.login, Search.search("stilling"), Search.search("arbeidserfaring"), Search.search("utdanning"), Search.search("kompetanse"), Search.search("geografi"), Search.search("år"), Search.search("utdanningsnivå"), Search.search("språk"))

  val searchStilling = scenario("Search - stilling")
    .feed(stillingInput)
    .exec(Login.login, Search.search("stilling"))

  val searchArbeidserfaring = scenario("Search - arbeidserfaring")
    .feed(stillingInput)
    .exec(Login.login, Search.search("arbeidserfaring"))

  val searchUtdanning = scenario("Search - utdanning")
    .feed(utdanningInput)
    .exec(Login.login, Search.search("utdanning"))

  val searchKompetanse = scenario("Search - kompetanse")
    .feed(kompetanseInput)
    .exec(Login.login, Search.search("kompetanse"))

  val searchGeografi = scenario("Search - geografi")
    .feed(geografiInput)
    .exec(Login.login, Search.search("geografi"))

  val searchAr = scenario("Search - år med erfaring")
    .feed(arbeidserfaringArInput)
    .exec(Login.login, Search.search("år"))

  val searchUtdanningsNiva = scenario("Search - utdanningsnivå")
    .feed(stillingInput)
    .exec(Login.login, Search.search("utdanningsnivå"))

  val searchSprak = scenario("Search - språk")
    .feed(sprakInput)
    .exec(Login.login, Search.search("språk"))

  setUp(
    // firstPage.inject(atOnceUsers(2)).protocols(httpProtocol),
    // typeahead.inject(atOnceUsers(80)).protocols(httpProtocol),
    // typeaheadStilling.inject(atOnceUsers(1), nothingFor(5), atOnceUsers(10), nothingFor(5), atOnceUsers(50)).protocols(httpProtocol),
    // typeaheadGeografi.inject(atOnceUsers(1)).protocols(httpProtocol),
    // typeaheadKompetanse.inject(atOnceUsers(1)).protocols(httpProtocol),
    search.inject(atOnceUsers(10)).protocols(httpProtocol)
    // searchStilling.inject(atOnceUsers(40)).protocols(httpProtocol),
    // searchKompetanse.inject(atOnceUsers(40)).protocols(httpProtocol),
    // searchGeografi.inject(atOnceUsers(20)).protocols(httpProtocol)
  )
  // setUp(scn.inject(rampUsers(50) over (10 seconds))).protocols(httpProtocol)
}
