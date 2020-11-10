package org.mierzwid.pact.demo.currency.nbp

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.mierzwid.pact.demo.currency.Code
import org.mierzwid.pact.demo.currency.NotFoundException
import org.mierzwid.pact.demo.currency.Rate
import org.mierzwid.pact.demo.currency.RateClient
import java.io.FileNotFoundException
import java.net.URL

class RateClientNbp(private val url: String = "http://api.nbp.pl") : RateClient {

    override fun getCurrencyToPlnRate(code: Code): Rate {
        return try {
            val responseText = URL(requestUrl(code)).readText()
            Json { ignoreUnknownKeys = true }
                .decodeFromString<RatesNbp>(responseText)
                .rates
                .map { Rate(it.mid.toBigDecimal()) }
                .first()
        } catch (e: FileNotFoundException) {
            throw NotFoundException()
        }
    }

    private fun requestUrl(code: Code) =
        URL_TEMPLATE.replace("PROVIDER_URL", url)
            .replace("CODE", code.toString())
            .replace("FORMAT", FORMAT)

    companion object {
        const val FORMAT = "json"
        const val URL_TEMPLATE = "PROVIDER_URL/api/exchangerates/rates/A/CODE?format=FORMAT"
    }
}

