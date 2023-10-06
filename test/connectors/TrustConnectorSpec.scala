/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import generators.Generators
import models.DeedOfVariation.PreviouslyAbsoluteInterestUnderWill
import models.settlors.{BusinessSettlor, DeceasedSettlor, IndividualSettlor, Settlors}
import models.{CompanyType, Name, RemoveSettlor, SettlorType, TaxableMigrationFlag, TrustDetails, TypeOfTrust, YesNoDontKnow}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsBoolean, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class TrustConnectorSpec extends SpecBase with Generators with ScalaFutures
  with Inside with BeforeAndAfterAll with BeforeAndAfterEach with IntegrationPatience {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  protected val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  val identifier = "1000000008"
  val index = 0
  val description = "description"
  val date: LocalDate = LocalDate.parse("2019-02-03")

  private val trustsUrl: String = "/trusts"
  private val settlorsUrl: String = s"$trustsUrl/settlors"

  private def getTrustDetailsUrl(identifier: String) = s"$trustsUrl/trust-details/$identifier/transformed"
  private def getSettlorsUrl(identifier: String) = s"$settlorsUrl/$identifier/transformed"
  private def getIsDeceasedSettlorDateOfDeathRecordedUrl(identifier: String) = s"$settlorsUrl/$identifier/transformed/deceased-settlor-death-recorded"
  private def addIndividualSettlorUrl(identifier: String) = s"$settlorsUrl/add-individual/$identifier"
  private def amendIndividualSettlorUrl(identifier: String, index: Int) = s"$settlorsUrl/amend-individual/$identifier/$index"
  private def addBusinessSettlorUrl(identifier: String) = s"$settlorsUrl/add-business/$identifier"
  private def amendBusinessSettlorUrl(identifier: String, index: Int) = s"$settlorsUrl/amend-business/$identifier/$index"
  private def amendDeceasedSettlorUrl(identifier: String) = s"$settlorsUrl/amend-deceased/$identifier"
  private def removeSettlorUrl(identifier: String) = s"$settlorsUrl/$identifier/remove"
  private def isTrust5mldUrl(identifier: String) = s"$trustsUrl/$identifier/is-trust-5mld"
  private def getTrustMigrationFlagUrl(identifier: String) = s"$trustsUrl/$identifier/taxable-migration/migrating-to-taxable"

  private val individual = IndividualSettlor(
    name = Name("Carmel", None, "Settlor"),
    dateOfBirth = None,
    countryOfNationality = None,
    countryOfResidence = None,
    identification = None,
    address = None,
    mentalCapacityYesNo = Some(YesNoDontKnow.DontKnow),
    entityStart = date,
    provisional = false
  )

  private val business = BusinessSettlor(
    name = "Settlor Org 24",
    companyType = Some(CompanyType.Investment),
    companyTime = Some(false),
    utr = None,
    address = None,
    entityStart = date,
    provisional = false
  )

  private val deceased = DeceasedSettlor(
    bpMatchStatus = None,
    name = Name("Carmel", None, "Settlor"),
    dateOfBirth = None,
    dateOfDeath = None,
    nationality = None,
    countryOfResidence = None,
    identification = None,
    address = None
  )

  "trust connector" when {

    "getTrustsDetails" in {

      val json = Json.parse(
        """
          |{
          | "startDate": "2019-02-03",
          | "lawCountry": "AD",
          | "administrationCountry": "GB",
          | "residentialStatus": {
          |   "uk": {
          |     "scottishLaw": false,
          |     "preOffShore": "AD"
          |   }
          | },
          | "typeOfTrust": "Will Trust or Intestacy Trust",
          | "deedOfVariation": "Previously there was only an absolute interest under the will",
          | "interVivos": false
          |}
          |""".stripMargin)

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        get(urlEqualTo(getTrustDetailsUrl(identifier)))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getTrustDetails(identifier)

      whenReady(processed) {
        r =>
          r mustBe TrustDetails(startDate = date, typeOfTrust = Some(TypeOfTrust.WillTrustOrIntestacyTrust), Some(PreviouslyAbsoluteInterestUnderWill), None)
      }

    }

    "getSettlors" when {

      "there are no settlors" must {

        "return a default empty list of settlors" in {

          val json = Json.parse(
            """
              |{
              | "settlors": {
              | }
              |}
              |""".stripMargin)

          val application = applicationBuilder()
            .configure(
              Seq(
                "microservice.services.trusts.port" -> server.port(),
                "auditing.enabled" -> false
              ): _*
            ).build()

          val connector = application.injector.instanceOf[TrustConnector]

          server.stubFor(
            get(urlEqualTo(getSettlorsUrl(identifier)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.getSettlors(identifier)

          whenReady(processed) {
            result =>
              result mustBe Settlors(settlor = Nil, settlorCompany = Nil, None)
          }

          application.stop()
        }
      }

      "there are settlors" must {

        "parse the response and return the settlors" in {

          val json = Json.parse(
            """
              |{
              | "settlors" : {
              |   "settlor" : [
              |     {
              |       "lineNo" : "79",
              |       "name" : {
              |         "firstName" : "Carmel",
              |         "lastName" : "Settlor"
              |       },
              |       "entityStart" : "2019-02-03"
              |     }
              |   ],
              |   "settlorCompany" : [
              |     {
              |       "lineNo" : "110",
              |       "bpMatchStatus" : "98",
              |       "name" : "Settlor Org 24",
              |       "companyType" : "Investment",
              |       "companyTime" : false,
              |       "entityStart" : "2019-02-03"
              |     }
              |   ],
              |    "deceased" : {
              |       "name" : {
              |         "firstName" : "Carmel",
              |         "lastName" : "Settlor"
              |       }
              |     }
              | }
              |}
              |""".stripMargin)

          val application = applicationBuilder()
            .configure(
              Seq(
                "microservice.services.trusts.port" -> server.port(),
                "auditing.enabled" -> false
              ): _*
            ).build()

          val connector = application.injector.instanceOf[TrustConnector]

          server.stubFor(
            get(urlEqualTo(getSettlorsUrl(identifier)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.getSettlors(identifier)

          whenReady(processed) {
            result =>
              result mustBe Settlors(
                settlor = List(individual),
                settlorCompany = List(business),
                deceased = Some(deceased)
              )
          }

          application.stop()
        }
      }
    }

    "addIndividualSettlor" must {

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(addIndividualSettlorUrl(identifier)))
            .willReturn(ok)
        )

        val result = connector.addIndividualSettlor(identifier, individual)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(addIndividualSettlorUrl(identifier)))
            .willReturn(badRequest)
        )

        val result = connector.addIndividualSettlor(identifier, individual)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "amendIndividualSettlor" must {

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendIndividualSettlorUrl(identifier, index)))
            .willReturn(ok)
        )

        val result = connector.amendIndividualSettlor(identifier, index, individual)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendIndividualSettlorUrl(identifier, index)))
            .willReturn(badRequest)
        )

        val result = connector.amendIndividualSettlor(identifier, index, individual)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "addBusinessSettlor" must {

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(addBusinessSettlorUrl(identifier)))
            .willReturn(ok)
        )

        val result = connector.addBusinessSettlor(identifier, business)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(addBusinessSettlorUrl(identifier)))
            .willReturn(badRequest)
        )

        val result = connector.addBusinessSettlor(identifier, business)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "amendBusinessSettlor" must {

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendBusinessSettlorUrl(identifier, index)))
            .willReturn(ok)
        )

        val result = connector.amendBusinessSettlor(identifier, index, business)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendBusinessSettlorUrl(identifier, index)))
            .willReturn(badRequest)
        )

        val result = connector.amendBusinessSettlor(identifier, index, business)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "amendDeceasedSettlor" must {

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendDeceasedSettlorUrl(identifier)))
            .willReturn(ok)
        )

        val result = connector.amendDeceasedSettlor(identifier, deceased)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendDeceasedSettlorUrl(identifier)))
            .willReturn(badRequest)
        )

        val result = connector.amendDeceasedSettlor(identifier, deceased)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }
    }

    "getIsDeceasedSettlorDateOfDeathRecorded" must {

      "Return true or false when the request is successful" in {

        val json = JsBoolean(true)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(getIsDeceasedSettlorDateOfDeathRecordedUrl(identifier)))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.getIsDeceasedSettlorDateOfDeathRecorded(identifier)

        whenReady(processed) {
          result =>
            result mustBe true
        }

        application.stop()
      }
    }

    "removeSettlor" must {

      def removeSettlor(settlorType: SettlorType): RemoveSettlor = RemoveSettlor(settlorType, index, date)

      "Return OK when the request is successful" in {

        forAll(arbitrarySettlorType) {
          settlorType =>

            val application = applicationBuilder()
              .configure(
                Seq(
                  "microservice.services.trusts.port" -> server.port(),
                  "auditing.enabled" -> false
                ): _*
              ).build()

            val connector = application.injector.instanceOf[TrustConnector]

            server.stubFor(
              put(urlEqualTo(removeSettlorUrl(identifier)))
                .willReturn(ok)
            )

            val result = connector.removeSettlor(identifier, removeSettlor(settlorType))

            result.futureValue.status mustBe OK

            application.stop()
        }
      }

      "return Bad Request when the request is unsuccessful" in {

        forAll(arbitrarySettlorType) {
          settlorType =>

            val application = applicationBuilder()
              .configure(
                Seq(
                  "microservice.services.trusts.port" -> server.port(),
                  "auditing.enabled" -> false
                ): _*
              ).build()

            val connector = application.injector.instanceOf[TrustConnector]

            server.stubFor(
              put(urlEqualTo(removeSettlorUrl(identifier)))
                .willReturn(badRequest)
            )

            val result = connector.removeSettlor(identifier, removeSettlor(settlorType))

            result.map(response => response.status mustBe BAD_REQUEST)

            application.stop()
        }
      }

    }

    "isTrust5mld" must {

      "return true" when {
        "untransformed data is 5mld" in {

          val json = JsBoolean(true)

          val application = applicationBuilder()
            .configure(
              Seq(
                "microservice.services.trusts.port" -> server.port(),
                "auditing.enabled" -> false
              ): _*
            ).build()

          val connector = application.injector.instanceOf[TrustConnector]

          server.stubFor(
            get(urlEqualTo(isTrust5mldUrl(identifier)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.isTrust5mld(identifier)

          whenReady(processed) {
            r =>
              r mustBe true
          }
        }
      }

      "return false" when {
        "untransformed data is 4mld" in {

          val json = JsBoolean(false)

          val application = applicationBuilder()
            .configure(
              Seq(
                "microservice.services.trusts.port" -> server.port(),
                "auditing.enabled" -> false
              ): _*
            ).build()

          val connector = application.injector.instanceOf[TrustConnector]

          server.stubFor(
            get(urlEqualTo(isTrust5mldUrl(identifier)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.isTrust5mld(identifier)

          whenReady(processed) {
            r =>
              r mustBe false
          }
        }
      }
    }

    "getTrustMigrationFlag" when {

      "value defined" in {

        val json = Json.parse(
          """
            |{
            | "value": true
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(getTrustMigrationFlagUrl(identifier)))
            .willReturn(okJson(json.toString))
        )

        val result = connector.getTrustMigrationFlag(identifier)

        whenReady(result) { r =>
          r mustBe TaxableMigrationFlag(Some(true))
        }

        application.stop()
      }

      "value undefined" in {

        val json = Json.parse(
          """
            |{}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(getTrustMigrationFlagUrl(identifier)))
            .willReturn(okJson(json.toString))
        )

        val result = connector.getTrustMigrationFlag(identifier)

        whenReady(result) { r =>
          r mustBe TaxableMigrationFlag(None)
        }

        application.stop()
      }
    }
  }
}
