package simulation

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

import scala.collection.immutable.{ListMap, TreeMap}

class PerformanceChallenge extends Simulation {

	val httpProtocol = http
		.baseUrl("https://challengers.flood.io")
		.disableFollowRedirect
		.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:70.0) Gecko/20100101 Firefox/70.0")

	val headers_0 = Map("Upgrade-Insecure-Requests" -> "1")

	val headers_1 = Map(
		"Origin" -> "https://challengers.flood.io",
		"Upgrade-Insecure-Requests" -> "1")

	val headers_2 = Map(
		"Accept-Encoding" -> "gzip, deflate",
		"Upgrade-Insecure-Requests" -> "1")

	val headers_13 = Map(
		"Accept" -> "*/*",
		"X-Requested-With" -> "XMLHttpRequest")

	val scn = scenario("PerformanceChallenge")

		// MainPage
		.exec(http("open main page")
			.get("/")
			.headers(headers_0)
			.check(regex("""<input name="authenticity_token" type="hidden" value="(.*?)" />""").saveAs("token"))
			.check(regex("step_id.*value=\"(.*?)\"").saveAs("stepIDForStep1")))
			.pause(3, 15)

		.exec(http("click start button")
			.post("/start")
			.headers(headers_1)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${token}")
			.formParam("challenger[step_id]", "${stepIDForStep1}")
			.formParam("challenger[step_number]", "1")
			.formParam("commit", "Start")
		  .check(status.is(302)))
		  .pause(3, 15)

		.exec(http("get step2")
			.get("/step/2")
			.check(status.is(200))
			.check(regex("step_id.*value=\"(.*?)\"").saveAs("stepIDForStep2"))
			.check(regex(">(24)<").saveAs("age"))
		  )
	  	.pause(3, 15)

		// Step2
		.exec(http("choose age")
			.post("/start")
			.headers(headers_1)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${token}")
			.formParam("challenger[step_id]", "${stepIDForStep2}")
			.formParam("challenger[step_number]", "2")
			.formParam("challenger[age]", "${age}")
			.formParam("commit", "Next")
			.check(status.is(302)))
		  .pause(3, 15)

	  .exec(http("get step 3")
		  .get("/step/3")
		  .check(status.is(200))
			.check(regex("step_id.*value=\"(.*?)\"").saveAs("stepIDForStep3"))
		  .check(regex("challenger_order_selected.*value=\"(.*?)\" ").findAll.saveAs("orderSelected"))
		  .check(regex(">(\\d+)<").findAll.saveAs("orderNumber")))
	    .pause(3, 15)

	.exec{ session => val listCollection= session("orderNumber").as[List[String]]
		val order  = session("orderSelected").as[List[String]]
		val intList=listCollection.map(_.toString.toInt)
		val m = (intList zip order).toMap
		val res = m.maxBy(_._1)

			session
				.set("maxNumber",res._2)
				.set("maxValue", res._1)
	}

		// Step3
		.exec(http("select largest order")
			.post("/start")
			.headers(headers_1)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${token}")
			.formParam("challenger[step_id]", "${stepIDForStep3}")
			.formParam("challenger[step_number]", "3")
			.formParam("challenger[largest_order]", "${maxValue}")
			.formParam("challenger[order_selected]", "${maxNumber}")
			.formParam("commit", "Next")
			.check(status.is(302)))
			.pause(3, 15)

		.exec(http("get step 4")
		  .get("/step/4")
	    .check(status.is(200))
			.check(regex("step_id.*value=\"(.*?)\"").saveAs("stepIDForStep4"))
		  .check(regex("challenger_order.*value=\"(\\d+)\"").saveAs("orderNumber")))
		  .pause(3, 15)

		// Step4
		.exec(http("click next")
			.post("/start")
			.headers(headers_1)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${token}")
			.formParam("challenger[step_id]", "${stepIDForStep4}")
			.formParam("challenger[step_number]", "4")
			.formParam("challenger[order_4]", "${orderNumber}")
			.formParam("challenger[order_1]", "${orderNumber}")
			.formParam("challenger[order_9]", "${orderNumber}")
			.formParam("challenger[order_12]", "${orderNumber}")
			.formParam("challenger[order_12]", "${orderNumber}")
			.formParam("challenger[order_12]", "${orderNumber}")
			.formParam("challenger[order_6]", "${orderNumber}")
			.formParam("challenger[order_9]", "${orderNumber}")
			.formParam("challenger[order_11]", "${orderNumber}")
			.formParam("challenger[order_13]", "${orderNumber}")
			.formParam("commit", "Next")
			.check(status.is(302)))
			.pause(3, 15)

		.exec(http("get step 5")
			.get("/step/5")
		  .check(status.is(200))
		  .check(regex("step_id.*value=\"(.*?)\"").saveAs("stepIDForStep5")))
		  .pause(3, 15)

		.exec(http("get one time token")
			.get("/code")
			.headers(headers_13)
			.check(status.is(200))
		  .check(regex("(\\d+)").saveAs("oneTimeToken")))
  		.pause(3, 15)

		// Step5
		.exec(http("select one time token")
			.post("/start")
			.headers(headers_1)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${token}")
			.formParam("challenger[step_id]", "${stepIDForStep5}")
			.formParam("challenger[step_number]", "5")
			.formParam("challenger[one_time_token]", "${oneTimeToken}")
			.formParam("commit", "Next")
			.check(status.is(302)))
			.pause(3, 15)

			.exec(http("done")
			.get("/done")
			.check(status.is(200)))

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}