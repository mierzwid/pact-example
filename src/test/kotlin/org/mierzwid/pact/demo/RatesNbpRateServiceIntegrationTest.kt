package org.mierzwid.pact.demo

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mierzwid.pact.demo.currency.Code
import org.mierzwid.pact.demo.currency.Rate
import org.mierzwid.pact.demo.currency.RateClient
import org.mierzwid.pact.demo.currency.RateService

class RatesNbpRateServiceIntegrationTest {

    @Test
    fun shouldReturnUsdToEurRate() {
        // given
        val service = RateService(object : RateClient {
            override fun getCurrencyToPlnRate(code: Code) =
                when (code) {
                    Code.USD -> Rate(5.0)
                    Code.EUR -> Rate(4.0)
                    else -> throw Exception("It should never ask for different rates")
                }
        })

        // when
        val usdToEurRate = service.usdToEurRate()

        // then
        assertEquals(1.25, usdToEurRate, 0.001)
    }
}