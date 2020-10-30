package com.cognifide.pact.demo

import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit.PactProviderRule
import au.com.dius.pact.consumer.junit.PactVerification
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.apache.http.entity.ContentType
import org.junit.Rule
import org.junit.Test


class NbpClientTest {

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
                    .stringValue("table", "A")
                    .stringValue("currency", "euro")
                    .stringValue("code", "EUR")
                    .array("rates")
                    .`object`()
                    .stringValue("no", "212/A/NBP/2020")
                    .stringMatcher("no", ".+")
                    .stringValue("effectiveDate", "2020-10-29")
                    .stringMatcher("effectiveDate", "[0-9]{4}/-[0-9]{2}/-[0-9]{4}")
                    .numberValue("mid", 4.6330)
                    .closeObject()
                    .closeArray()
            )
            .toPact()
    }

    @Test
    @PactVerification(value = ["nbp"], fragment = "nbpEurPact")
    fun shouldFetchEURRatesPACT() {
        //given
        val client = NbpClient(provider.url)

        //when
        val rate = client.getRate(Code.EUR)

        //then
        assertEquals(4.6330, rate)
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
            .body("404 NotFound - Not Found - Brak danych", ContentType.TEXT_PLAIN)
            .toPact()
    }

    @Test
    @PactVerification(value = ["nbp"], fragment = "nbp404Pact")
    fun shouldRespondWith404PACT() {
        //given
        val client = NbpClient(provider.url)

        //when
        try {
            client.getRate(Code.UNSPECIFIED)

            //then
            fail("Expected exception to be thrown")
        } catch (e: NotFoundException) {
            //pass
        }
    }

    @Test
    fun shouldFetchUSDRates() {
        val rate = NbpClient().getRate(Code.USD)

        assertEquals(3.9563, rate)
    }

    @Test
    fun shouldFetchEURRates() {
        val rate = NbpClient().getRate(Code.EUR)

        assertEquals(4.6188, rate)
    }
}