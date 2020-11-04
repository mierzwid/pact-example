package org.mierzwid.pact.demo

import org.mierzwid.pact.demo.rates.Code
import org.mierzwid.pact.demo.rates.nbp.RateClientNbp
import org.mierzwid.pact.demo.rates.NotFoundException

fun main() {
    val client = RateClientNbp("http://localhost:8910")
    val rate = client.getRate(Code.EUR)
    println("This is some rate: ${rate.mid}")
    try {
        client.getRate(Code.UNSPECIFIED)
    } catch (e: NotFoundException) {
        println("Truly 404 when bad request: $e")
    }
}
