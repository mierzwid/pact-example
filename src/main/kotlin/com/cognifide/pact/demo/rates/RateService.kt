package com.cognifide.pact.demo.rates

import com.cognifide.pact.demo.rates.nbp.RateClientNbp

class RateService(private val client: RateClient = RateClientNbp()) {

    fun usdToEurRate(): Double {
        val usdRate = client.getRate(Code.USD)
        val eurRate = client.getRate(Code.EUR)
        return usdRate.mid / eurRate.mid
    }
}