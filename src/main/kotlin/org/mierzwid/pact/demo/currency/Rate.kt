package org.mierzwid.pact.demo.currency

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

data class Rate(val value: BigDecimal) {
    operator fun div(rate: Rate) = Rate(value.divide(rate.value, MATH_CONTEXT))

    companion object {
        private val MATH_CONTEXT = MathContext(3, RoundingMode.CEILING)
        fun of(mid: Double) = Rate(mid.toBigDecimal(MATH_CONTEXT))
    }
}