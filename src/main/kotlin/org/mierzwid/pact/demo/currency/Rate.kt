package org.mierzwid.pact.demo.currency

import java.math.BigDecimal
import java.math.MathContext

data class Rate(val value: BigDecimal) {
    operator fun div(rate: Rate) = Rate(value.divide(rate.value, MathContext.UNLIMITED))

    companion object {
        fun of(mid: Double) = Rate(mid.toBigDecimal(MathContext.UNLIMITED))
    }
}