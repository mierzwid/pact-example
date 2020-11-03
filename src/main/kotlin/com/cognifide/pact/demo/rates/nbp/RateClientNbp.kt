package com.cognifide.pact.demo.rates.nbp

import com.cognifide.pact.demo.rates.Code
import com.cognifide.pact.demo.rates.ExchangeRates
import com.cognifide.pact.demo.rates.NotFoundException
import com.cognifide.pact.demo.rates.RateClient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.net.URL

class RateClientNbp(private val url: String = "http://api.nbp.pl") : RateClient {

    override fun getRate(code: Code) = try {
        val responseText = URL(requestUrl(code)).readText()
        Json.decodeFromString<ExchangeRates>(responseText).rates.first()
    } catch (e: FileNotFoundException) {
        throw NotFoundException()
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
