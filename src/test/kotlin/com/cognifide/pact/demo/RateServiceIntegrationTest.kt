package com.cognifide.pact.demo

import com.cognifide.pact.demo.rates.Code
import com.cognifide.pact.demo.rates.Rate
import com.cognifide.pact.demo.rates.RateClient
import com.cognifide.pact.demo.rates.RateService
import org.junit.Assert.assertEquals
import org.junit.Test

class RateServiceIntegrationTest {

    @Test
    fun shouldReturnUsdToEurRate() {
        // given
        val client = object : RateClient {
            override fun getRate(code: Code) =
                when (code) {
                    Code.USD -> Rate.of(5.0)
                    Code.EUR -> Rate.of(4.0)
                    else -> throw Exception("It should never ask for different rates")
                }
        }
        val service = RateService(client)

        // when
        val usdToEurRate = service.usdToEurRate()

        // then
        assertEquals(1.25, usdToEurRate, 0.001)
    }
}