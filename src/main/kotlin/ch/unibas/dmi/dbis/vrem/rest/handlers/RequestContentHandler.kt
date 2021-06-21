package ch.unibas.dmi.dbis.vrem.rest.handlers

import io.javalin.http.Context
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path

/**
 * Handler for content requests made through the API.
 *
 * @property docRoot The document root of the exhibition.
 * @constructor
 */
class RequestContentHandler(private val docRoot: Path) {

    companion object {
        private val LOGGER = LogManager.getLogger(RequestContentHandler::class.java)
        const val PARAM_KEY_PATH = ":path"
    }

    /**
     * Serves the requested content.
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

        ctx.result(absolute.toFile().inputStream())
    }

}
