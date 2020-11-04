package org.mierzwid.pact.demo.rates

interface RateClient {

    fun getRate(code: Code): Rate
}