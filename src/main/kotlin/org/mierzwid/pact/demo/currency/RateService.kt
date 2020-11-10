package org.mierzwid.pact.demo.currency

import org.mierzwid.pact.demo.currency.nbp.RateClientNbp

class RateService(private val client: RateClient = RateClientNbp()) {

    fun usdToEurRate(): Rate {
        val usd = client.getCurrencyToPlnRate(Code.USD)
        val eur = client.getCurrencyToPlnRate(Code.EUR)
        return usd / eur
    }
}