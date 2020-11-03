package com.cognifide.pact.demo.rates

interface RateClient {

    fun getRate(code: Code): Rate
}