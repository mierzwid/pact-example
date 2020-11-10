package org.mierzwid.pact.demo.currency

import org.mierzwid.pact.demo.currency.nbp.RateClientNbp

class RateService(private val client: RateClient = RateClientNbp()) {

    fun usdToEurRate(): Double {
        val usdRate = client.getCurrencyToPlnRate(Code.USD)
        val eurRate = client.getCurrencyToPlnRate(Code.EUR)
        return usdRate.mid / eurRate.mid
    }
}