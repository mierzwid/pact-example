package org.mierzwid.pact.demo

import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit.PactProviderRule
import au.com.dius.pact.consumer.junit.PactVerification
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.apache.commons.io.ByteOrderMark.UTF_BOM
import org.apache.http.entity.ContentType
import org.junit.Rule
import org.junit.Test
import org.mierzwid.pact.demo.currency.Code
import org.mierzwid.pact.demo.currency.NotFoundException
import org.mierzwid.pact.demo.currency.Rate
import org.mierzwid.pact.demo.currency.nbp.RateClientNbp
import java.sql.Date
import java.time.LocalDate


class RatesNbpRateClientPactTest {

    @get:Rule
    var provider: PactProviderRule = PactProviderRule("nbp", "localhost", 1234, this)

    @Pact(provider = "nbp", consumer = "demo")
    fun nbpEurPact(builder: PactDslWithProvider): RequestResponsePact? {
        return builder
            .given("euro rate")
            .uponReceiving("a request for json data")
            .path("/api/exchangerates/rates/A/EUR")
            .method("GET")
            .query("format=json")
            .willRespondWith()
            .status(200)
            .body(
                PactDslJsonBody()
                    .stringValue("code", "EUR")
                    .array("rates")
                    .`object`()
                    .numberType("mid", 4.38)
                    .closeObject()
                    .closeArray()
            )
            .toPact()
    }

    @Test
    @PactVerification(value = ["nbp"], fragment = "nbpEurPact")
    fun shouldFetchEURRatesPACT() {
        //given
        val client = RateClientNbp(provider.url)

        //when
        val rate = client.getCurrencyToPlnRate(Code.EUR)

        //then
        assertEquals(Rate.of(4.38), rate)
    }

    @Pact(provider = "nbp", consumer = "demo")
    fun nbp404Pact(builder: PactDslWithProvider): RequestResponsePact? {
        return builder
            .given("invalid currency code")
            .uponReceiving("a request for json data")
            .path("/api/exchangerates/rates/A/UNSPECIFIED")
            .method("GET")
            .query("format=json")
            .willRespondWith()
            .status(404)
            .body("${UTF_BOM}404 NotFound", ContentType.create("text/plain", "UTF-8"))
            .toPact()
    }

    @Test
    @PactVerification(value = ["nbp"], fragment = "nbp404Pact")
    fun shouldRespondWith404PACT() {
        //given
        val client = RateClientNbp(provider.url)

        //when
        try {
            client.getCurrencyToPlnRate(Code.UNSPECIFIED)

            //then
            fail("Expected exception to be thrown")
        } catch (e: NotFoundException) {
            //pass
        }
    }

    private fun date(year: Int, month: Int, day: Int) = Date.valueOf(LocalDate.of(year, month, day))
}