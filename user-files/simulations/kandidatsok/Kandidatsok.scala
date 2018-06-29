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

  val env = t6

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
  val arbeidserfaringInput = csv("kandidatsokArbeidserfaring.csv").random
  val kompetanseInput = csv("kandidatsokKompetanse.csv").random
  val geografiInput = csv("kandidatsokGeografi.csv").random

  // Cookie value - Denne må genereres manuelt i en browser før en kjøring, og limes inn her
  val cookieValue = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImZ5akpfczQwN1ZqdnRzT0NZcEItRy1IUTZpYzJUeDNmXy1JT3ZqVEFqLXcifQ.eyJleHAiOjE1MzAyODY4NTksIm5iZiI6MTUzMDI4MzI1OSwidmVyIjoiMS4wIiwiaXNzIjoiaHR0cHM6Ly9sb2dpbi5taWNyb3NvZnRvbmxpbmUuY29tL2QzOGYyNWFhLWVhYjgtNGM1MC05ZjI4LWViZjkyYzEyNTZmMi92Mi4wLyIsInN1YiI6IjA4MDQ0NjAxOTc1IiwiYXVkIjoiMDA5MGI2ZTEtZmZjYy00YzM3LWJjMjEtMDQ5ZjdkMWYwZmU1IiwiYWNyIjoiTGV2ZWw0Iiwibm9uY2UiOiJoQkEySVc0T0Z4RzBNVG5kNDc5VDAxYWxxck1tTnJleXVxZXdHZGF3QnBzIiwiaWF0IjoxNTMwMjgzMjU5LCJhdXRoX3RpbWUiOjE1MzAyODMyNTksImp0aSI6InQ4VklneDk1UGJneGtnanpIcE44eXBsUXFWcTJNZ1RuWjlZZ3N4Y3R0Rnc9IiwiYXRfaGFzaCI6Ik0tS1hKN3dfS3Q0Nnpsc28yVEFBQ2cifQ.jpTDGPg7vwYhuoTHcbk6SxepCUN3i4MwM0oychiofSiNmWcC0tle7jew81VNLnZgOL0CFmRthdGHd5717ulPNhbKes9koRZ-a1IY_MWzFwKlW9-9nP83sDhFJw81efZZ_InLLJKUSzKs02urrA4VFrQO5OBngB5PThIsoVANCfEvInvVALhY8Pssxy3PxBdIC0Nvwsh68eCr92KcavVV0Wn-JMoIP72p_BAb1zcwwzw9EmE6vF3YbR1JPna9DBriqmZyFRZsM0cWcN8siyEZ7ccruDmoa08M_jRJevg83RCt-HZEh5eBjNVn147SvLIFoJIHFD4W74Fa4l4BQ6l0-g"

  val globalPause = 1 // Global pause mellom requests, i sekunder

  val scn = scenario("Kandidatsok")
    .feed(stillingInput)
    .feed(utdanningInput)
    .feed(arbeidserfaringInput)
    .feed(kompetanseInput)
    .feed(geografiInput)
    .exec(addCookie(Cookie("selvbetjening-idtoken", cookieValue)
      .withDomain(".nav.no")))
    .exec(http("1. Last forside")
      .get("/pam-kandidatsok")
      .headers(headers_0)
      .resources(http("2. Feature toggle")
        .get("/pam-kandidatsok/rest/kandidatsok/toggles?feature=vis-manglende-arbeidserfaring-boks,vis-ta-kontakt-kandidat"),
        http("3. Søk uten kriterier")
          .get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=&utdanningsniva=&sprak=")))
    .pause(globalPause)
    .exec(http("4. Typeahead stilling")
      .get("/pam-kandidatsok/rest/kandidatsok/typeahead?sti=${STILLINGTYPEAHEAD}"))
    .pause(globalPause)
    .exec(http("5. Nytt søk med stilling")
      .get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=&utdanningsniva=&sprak=")
      .resources(http("6. Forslag til kompetanse")
        .get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}")))
    .pause(globalPause)
    .exec(http("7. Legger til søk med utdanningsnivå")
      .get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=&utdanningsniva=${UTDANNINGSNIVA}&sprak="))
    .pause(globalPause)
    .exec(http("8. Legger til søk med år med erfaring")
      .get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=${AR}&utdanningsniva=${UTDANNINGSNIVA}&sprak="))
    .pause(globalPause)
    .exec(http("9. Nytt søk med kompetanse")
      .get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&arbeidserfaringer=&utdanninger=&kompetanser=${KOMPETANSE}&geografiList=&geografiListKomplett=&totalErfaring=${AR}&utdanningsniva=${UTDANNINGSNIVA}&sprak="))
    .pause(globalPause)
    .exec(http("10. Typeahead geografi")
      .get("/pam-kandidatsok/rest/kandidatsok/typeahead?geo=${GEOGRAFITYPEAHEAD}")
      .resources(http("11. Nytt totalsøk med geografi")
        .get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&&arbeidserfaringer=&utdanninger=&kompetanser=${KOMPETANSE}&geografiList=${GEOGRAFILISTE}&geografiListKomplett=%5Bobject+Object%5D&totalErfaring=${AR}&utdanningsniva=${UTDANNINGSNIVA}&sprak=")))
    .pause(globalPause)
    .exec(http("12. Klikk Se kandidater")
      .get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&&arbeidserfaringer=&utdanninger=&kompetanser=${KOMPETANSE}&geografiList=${GEOGRAFILISTE}&geografiListKomplett=%5Bobject+Object%5D&totalErfaring=${AR}&utdanningsniva=${UTDANNINGSNIVA}&sprak=")
      .resources(http("13. Forslag til kompetanse")
        .get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}")))

  setUp(scn.inject(rampUsers(10) over (10 seconds))).protocols(httpProtocol)
}
