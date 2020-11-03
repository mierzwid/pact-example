package com.cognifide.pact.demo.rates.mock

import com.cognifide.pact.demo.rates.Code
import com.cognifide.pact.demo.rates.NotFoundException
import com.cognifide.pact.demo.rates.Rate
import com.cognifide.pact.demo.rates.RateClient

class RateClientMock : RateClient {
    override fun getRate(code: Code) =
        when (code) {
            Code.USD -> Rate("1", "2020-01-01", 3.82)
            Code.EUR -> Rate("1", "2020-01-01", 4.674)
            Code.CHF -> Rate("1", "2020-01-01", 3.483)
            else -> throw NotFoundException()
        }
}