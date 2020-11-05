package org.mierzwid.pact.demo.rates.stub

import org.mierzwid.pact.demo.rates.Code
import org.mierzwid.pact.demo.rates.NotFoundException
import org.mierzwid.pact.demo.rates.Rate
import org.mierzwid.pact.demo.rates.RateClient

class RateClientStub : RateClient {
    override fun getRate(code: Code) =
        when (code) {
            Code.USD -> Rate(3.82)
            Code.EUR -> Rate(4.674)
            Code.CHF -> Rate(3.483)
            else -> throw NotFoundException()
        }
}