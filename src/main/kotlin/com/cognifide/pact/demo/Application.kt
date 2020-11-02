package com.cognifide.pact.demo

fun main(args: Array<String>) {
    val client = NbpClient("http://localhost:8080")
    val rate = client.getRate(Code.EUR)
    println("This is some rate: $rate")
    try {
        client.getRate(Code.UNSPECIFIED)
    } catch (e: NotFoundException) {
        println("Truly 404 when bad request: $e")
    }
}
