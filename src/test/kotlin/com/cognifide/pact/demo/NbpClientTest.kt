package com.cognifide.pact.demo

import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit.PactProviderRule
import au.com.dius.pact.consumer.junit.PactVerification
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import junit.framework.TestCase.assertEquals
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
                """
                {
                    "table": "A",
                    "currency": "euro",
                    "code": "EUR",
                    "rates": [
                    {
                        "no": "212/A/NBP/2020",
                        "effectiveDate": "2020-10-29",
                        "mid": 4.6330
                    }
                    ]
                }
                """.trimIndent()
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