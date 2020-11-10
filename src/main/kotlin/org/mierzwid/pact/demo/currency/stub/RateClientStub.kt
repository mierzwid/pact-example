package org.mierzwid.pact.demo.currency.stub

import org.mierzwid.pact.demo.currency.Code
import org.mierzwid.pact.demo.currency.NotFoundException
import org.mierzwid.pact.demo.currency.Rate
import org.mierzwid.pact.demo.currency.RateClient

class RateClientStub : RateClient {
    override fun getCurrencyToPlnRate(code: Code) =
        when (code) {
            Code.USD -> Rate.of(3.82)
            Code.EUR -> Rate.of(4.674)
            Code.CHF -> Rate.of(3.483)
            else -> throw NotFoundException()
        }
}