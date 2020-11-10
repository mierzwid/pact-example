package org.mierzwid.pact.demo.currency

import kotlinx.serialization.Serializable

@Serializable
data class Rate(val mid: Double)