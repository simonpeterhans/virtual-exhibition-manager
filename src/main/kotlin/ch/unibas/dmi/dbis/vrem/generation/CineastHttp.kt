package ch.unibas.dmi.dbis.vrem.generation

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import com.github.kittinunf.fuel.httpGet

@Suppress("HttpUrlsUsage")
class CineastHttp(private val dbConfig: CineastConfig) {

    fun objectRequest(id: String): ByteArray {
        val (_, _, result) = "http://${dbConfig.host}:${dbConfig.port}${dbConfig.objectPath}/$id"
            .httpGet()
            .response()

        return result.get()
    }

}
