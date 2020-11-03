package com.cognifide.pact.demo.rates.mock

import com.cognifide.pact.demo.rates.Code
import com.cognifide.pact.demo.rates.NotFoundException
import com.cognifide.pact.demo.rates.Rate
import com.cognifide.pact.demo.rates.RateClient

class RateClientMock : RateClient {
    override fun getRate(code: Code) =
        when (code) {
            Code.USD -> Rate.of(3.82)
            Code.EUR -> Rate.of(4.674)
            Code.CHF -> Rate.of(3.483)
            else -> throw NotFoundException()
        }
}