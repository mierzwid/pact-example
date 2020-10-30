package com.cognifide.pact.demo

import kotlinx.serialization.Serializable

@Serializable
data class Rate(
        val no: String,
        val effectiveDate: String,
        val mid: Double
)