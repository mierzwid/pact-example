package com.cognifide.pact.demo

import org.junit.jupiter.api.Test

class NbpClientTest {

    @Test
    fun test() {
        val rate = NbpClient().getRate(Code.USD)

        println("test result: $rate")
    }
}