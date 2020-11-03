package com.cognifide.pact.demo

import com.cognifide.pact.demo.rates.Code
import com.cognifide.pact.demo.rates.nbp.RateClientNbp
import com.cognifide.pact.demo.rates.NotFoundException

fun main() {
    val client = RateClientNbp("http://localhost:8080")
    val rate = client.getRate(Code.EUR)
    println("This is some rate: ${rate.mid}")
    try {
        client.getRate(Code.UNSPECIFIED)
    } catch (e: NotFoundException) {
        println("Truly 404 when bad request: $e")
    }
}
