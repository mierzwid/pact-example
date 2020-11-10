package org.mierzwid.pact.demo

import org.mierzwid.pact.demo.currency.RateService
import org.mierzwid.pact.demo.currency.stub.RateClientStub

fun main() {
    val env = Env.STAGE
    val service = when (env) {
        Env.INTEGRATION -> RateService(RateClientStub())
        else -> RateService()
    }
    val rate = service.usdToEurRate()
    println("This is some rate: $rate")
}
