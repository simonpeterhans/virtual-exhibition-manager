package ch.unibas.dmi.dbis.vrem.rest.handlers

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.javalin.http.Context
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayInputStream
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path

/**
 * Handler for content requests made through the API.
 *
 * @property docRoot The document root of the exhibition.
 * @constructor
 */
class RequestContentHandler(private val docRoot: Path, private val cineastConfig: CineastConfig) {

    companion object {
        const val PARAM_KEY_PATH = ":path"
        const val URL_ID_SUFFIX = ".remote"
        private val LOGGER = LogManager.getLogger(RequestContentHandler::class.java)
    }

    /**
     * Serves the requested content.
     *
     * TODO Clean this up!
     *
     * @param ctx The Javalin request context.
     */
    fun serveContent(ctx: Context) {
        val path = ctx.pathParam(PARAM_KEY_PATH)

        if (path.isBlank()) {
            LOGGER.error("The requested path was blank - did you forget to send the actual content path?")
            ctx.status(404)
            return
        }

        if (path.endsWith(URL_ID_SUFFIX)) {
            val id = path.substring(path.indexOf("/") + 1, path.indexOf(URL_ID_SUFFIX))
            var resultBytes: ByteArray? = null

            LOGGER.info("Trying to serve $id.")

            val (_, _, result) = cineastConfig.getCineastObjectUrlString(id).httpGet().response()

            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                    LOGGER.error("Cannot serve object with id $id: $ex.")
                    ctx.status(404)
                    return
                }
                is Result.Success -> {
                    resultBytes = result.get()
                }
            }

            ctx.contentType(URLConnection.guessContentTypeFromStream(ByteArrayInputStream(resultBytes)))
            ctx.header("Transfer-Encoding", "identity")
            ctx.header("Access-Control-Allow-Origin", "*")
            ctx.header("Access-Control-Allow-Headers", "*")

            ctx.result(resultBytes)
        } else {
            val absolute = docRoot.resolve(path)

            if (!Files.exists(absolute)) {
                LOGGER.error("Cannot serve $absolute as it does not exist.")
                ctx.status(404)
                return
            }

            ctx.contentType(Files.probeContentType(absolute))
            ctx.header("Transfer-Encoding", "identity")
            ctx.header("Access-Control-Allow-Origin", "*")
            ctx.header("Access-Control-Allow-Headers", "*")

            LOGGER.info("Serving $absolute.")

            ctx.result(absolute.toFile().inputStream())
        }
    }

}
