package org.mierzwid.pact.demo

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mierzwid.pact.demo.rates.Code
import org.mierzwid.pact.demo.rates.Rate
import org.mierzwid.pact.demo.rates.RateClient
import org.mierzwid.pact.demo.rates.RateService

class RateServiceIntegrationTest {

    @Test
    fun shouldReturnUsdToEurRate() {
        // given
        val service = RateService(object : RateClient {
            override fun getRate(code: Code) =
                when (code) {
                    Code.USD -> Rate.of(5.0)
                    Code.EUR -> Rate.of(4.0)
                    else -> throw Exception("It should never ask for different rates")
                }
        })

        // when
        val usdToEurRate = service.usdToEurRate()

        // then
        assertEquals(1.25, usdToEurRate, 0.001)
    }
}