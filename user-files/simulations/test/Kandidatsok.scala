package test

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class Kandidatsok extends Simulation {

	val httpProtocol = http
		.baseURL("https://tjenester-t6.nav.no")
		.inferHtmlResources(BlackList(""".*\.css""", """.*\.js""", """.*\.ico"""), WhiteList())
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.9,nb;q=0.8")
		.userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36")

	val headers_0 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
		"Upgrade-Insecure-Requests" -> "1")

    val uri1 = "https://tjenester-t6.nav.no:443/pam-kandidatsok"

	val scn = scenario("Kandidatsok")
		.exec(http("request_0")
			.get("/pam-kandidatsok")
			.headers(headers_0)
			.resources(http("request_1")
			.get("/pam-kandidatsok/rest/kandidatsok/toggles?feature=vis-manglende-arbeidserfaring-boks,vis-ta-kontakt-kandidat"),
            http("request_2")
			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=&utdanningsniva=&sprak=")))
		.pause(5)
		.exec(http("request_3")
			.get("/pam-kandidatsok/rest/kandidatsok/typeahead?sti=Ing"))
		.pause(2)
		.exec(http("request_4")
			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=Ingeni%C3%B8r&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=&utdanningsniva=&sprak=")
			.resources(http("request_5")
			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=Ingeni%C3%B8r")))
		.pause(3)
		.exec(http("request_6")
			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=Ingeni%C3%B8r&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=&utdanningsniva=Master&sprak="))
		.pause(8)
		.exec(http("request_7")
			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=Ingeni%C3%B8r&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=120-&utdanningsniva=Master&sprak="))
		.pause(8)
		.exec(http("request_8")
			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=Ingeni%C3%B8r&arbeidserfaringer=&utdanninger=&kompetanser=Prosjektarbeid&geografiList=&geografiListKomplett=&totalErfaring=120-&utdanningsniva=Master&sprak="))
		.pause(3)
		.exec(http("request_9")
			.get("/pam-kandidatsok/rest/kandidatsok/typeahead?geo=Osl")
			.resources(http("request_10")
			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=Ingeni%C3%B8r&arbeidserfaringer=&utdanninger=&kompetanser=Prosjektarbeid&geografiList=NO03.0301&geografiListKomplett=%5Bobject+Object%5D&totalErfaring=120-&utdanningsniva=Master&sprak=")))
		.pause(5)
		.exec(http("request_11")
			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=Ingeni%C3%B8r&arbeidserfaringer=&utdanninger=&kompetanser=Prosjektarbeid&geografiList=NO03.0301&geografiListKomplett=%5Bobject+Object%5D&totalErfaring=120-&utdanningsniva=Master&sprak=")
			.resources(http("request_12")
			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=Ingeni%C3%B8r")))

	// val scn2 = scenario("Kandidatsok2")
	// 	.feed(stillingInput)
	// 	.feed(utdanningInput)
	// 	.feed(arbeidserfaringInput)
	// 	.feed(kompetanseInput)
	// 	.feed(geografiInput)
	// 	.exec(addCookie(Cookie("selvbetjening-idtoken", cookieValue)
	// 		.withDomain(".nav.no")))
	// 	.exec(http("1. Last forside")
	// 		.get("/pam-kandidatsok")
	// 		.headers(headers_0)
	// 		.resources(http("2. Feature toggle")
	// 			.get("/pam-kandidatsok/rest/kandidatsok/toggles?feature=vis-manglende-arbeidserfaring-boks,vis-ta-kontakt-kandidat"),
	// 			http("3. Søk uten kriterier")
	// 			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=&utdanningsniva=&sprak=")))
	// 	.pause(globalPause)
	// 	.exec(http("4. Typeahead stilling")
	// 		.get("/pam-kandidatsok/rest/kandidatsok/typeahead?sti=${STILLINGTYPEAHEAD}"))
	// 	.pause(globalPause)
	// 	.exec(http("5. Nytt søk med stilling")
	// 		.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=&utdanningsniva=&sprak=")
	// 	.resources(http("6. Forslag til kompetanse")
	// 		.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}")))
	// 	.pause(globalPause)
	// 	.exec(http("7. Legger til søk med utdanningsnivå")
	// 		.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=&utdanningsniva=${UTDANNINGSNIVA}&sprak="))
	// 	.pause(globalPause)
	// 	.exec(http("8. Legger til søk med år med erfaring")
	// 		.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&arbeidserfaringer=&utdanninger=&kompetanser=&geografiList=&geografiListKomplett=&totalErfaring=${AR}&utdanningsniva=${UTDANNINGSNIVA}&sprak="))
	// 	.pause(globalPause)
	// 	.exec(http("9. Nytt søk med kompetanse")
	// 		.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&arbeidserfaringer=&utdanninger=&kompetanser=${KOMPETANSE}&geografiList=&geografiListKomplett=&totalErfaring=${AR}&utdanningsniva=${UTDANNINGSNIVA}&sprak="))
	// 	.pause(globalPause)
	// 	.exec(http("10. Typeahead geografi")
	// 		.get("/pam-kandidatsok/rest/kandidatsok/typeahead?geo=${GEOGRAFITYPEAHEAD}")
	// 		.resources(http("11. Nytt totalsøk med geografi")
	// 			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&arbeidserfaringer=&utdanninger=&kompetanser=${KOMPETANSE}&geografiList=${GEOGRAFILISTE}&geografiListKomplett=%5Bobject+Object%5D&totalErfaring=${AR}&utdanningsniva=${UTDANNINGSNIVA}&sprak=")))
	// 	.pause(globalPause)
	// 	.exec(http("12. Klikk Se kandidater")
	// 		.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}&arbeidserfaringer=&utdanninger=&kompetanser=${KOMPETANSE}&geografiList=${GEOGRAFILISTE}&geografiListKomplett=%5Bobject+Object%5D&totalErfaring=${AR}&utdanningsniva=${UTDANNINGSNIVA}&sprak=")
	// 		.resources(http("13. Forslag til kompetanse")
	// 			.get("/pam-kandidatsok/rest/kandidatsok/sok?stillinger=${STILLING}")))		

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}