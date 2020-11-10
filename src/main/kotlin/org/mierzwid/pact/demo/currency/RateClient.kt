package org.mierzwid.pact.demo.currency

interface RateClient {

    fun getCurrencyToPlnRate(code: Code): Rate
}