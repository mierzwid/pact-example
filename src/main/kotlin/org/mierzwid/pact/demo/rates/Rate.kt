package org.mierzwid.pact.demo.rates

import kotlinx.serialization.Serializable

@Serializable
data class Rate(
        val no: String,
        val effectiveDate: String,
        val mid: Double
) {
    companion object {
        fun of(mid: Double) = Rate("1", "2020-01-01", mid)
    }
}