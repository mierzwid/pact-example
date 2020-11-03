package com.cognifide.pact.demo.rates

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRates(
        val table: String,
        val currency: String,
        val code: String,
        val rates: List<Rate>
)