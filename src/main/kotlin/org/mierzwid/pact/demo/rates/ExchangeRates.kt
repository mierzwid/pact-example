package org.mierzwid.pact.demo.rates

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRates(
        val code: String,
        val rates: List<Rate>
)