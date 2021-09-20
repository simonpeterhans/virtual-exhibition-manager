package ch.unibas.dmi.dbis.vrem.generation.cineast

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import com.github.kittinunf.fuel.httpGet

@Suppress("HttpUrlsUsage")
class CineastHttp(private val cineastConfig: CineastConfig) {

    // Necessary since serving content is not actually (properly) included in Cineast's OpenAPI spec.
    fun objectRequest(id: String): ByteArray {
        val (_, _, result) = "http://${cineastConfig.host}:${cineastConfig.port}${cineastConfig.objectPath}/$id"
            .httpGet()
            .timeout(cineastConfig.queryTimeoutSeconds.toInt())
            .response()

        return result.get()
    }

}
