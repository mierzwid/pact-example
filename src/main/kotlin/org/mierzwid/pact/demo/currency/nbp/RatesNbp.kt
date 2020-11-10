package org.mierzwid.pact.demo.currency.nbp

import kotlinx.serialization.Serializable
import org.mierzwid.pact.demo.currency.Rate

@Serializable
data class RatesNbp(
        val code: String,
        val rates: List<RateNbp>
)

@Serializable
data class RateNbp(val mid: Double)