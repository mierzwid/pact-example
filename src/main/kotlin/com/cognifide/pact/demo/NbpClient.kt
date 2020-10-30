package com.cognifide.pact.demo

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URL

class NbpClient(private val url: String = "http://api.nbp.pl") {

    fun getRate(code: Code): Double {
        val deserializer = URL(requestUrl(code)).readText()
        val rates = Json.decodeFromString<ExchangeRates>(deserializer)
        return rates.rates.first().mid
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

