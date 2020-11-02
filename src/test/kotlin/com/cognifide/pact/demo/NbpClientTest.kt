package com.cognifide.pact.demo

import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit.PactProviderRule
import au.com.dius.pact.consumer.junit.PactVerification
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import junit.framework.TestCase.*
import org.apache.commons.io.ByteOrderMark.UTF_BOM
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
                    .stringMatcher("no", "[0-9]+/A/NBP/[0-9]{4}")
                    .date("effectiveDate", "yyyy-MM-dd")
                    .numberType("mid")
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
        assertNotNull(rate)
        assertTrue(rate > 0)
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
}