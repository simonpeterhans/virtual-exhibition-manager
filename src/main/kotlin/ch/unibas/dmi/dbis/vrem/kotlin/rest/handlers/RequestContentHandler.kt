package ch.unibas.dmi.dbis.vrem.kotlin.rest.handlers

import io.javalin.http.Context
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path

class RequestContentHandler(private val docRoot: Path) {

    private val LOGGER = LogManager.getLogger(RequestContentHandler::class.java)

    companion object {
        const val PARAM_KEY_PATH = ":path"
    }

    fun serveContent(ctx: Context){
        val path = ctx.pathParam(PARAM_KEY_PATH)

        if(path.isBlank()){
            LOGGER.error("The requested path was blank. Did you forget to send the actual content path? Sending 404")
            ctx.status(404)
            return
        }

        val absolute = docRoot.resolve(path)
        if(!Files.exists(absolute)){
            LOGGER.error("Cannot serve $absolute, as it does not exist. Sending 404")
            ctx.status(404)
        }

        ctx.contentType(Files.probeContentType(absolute))
        ctx.header("Transfer-Encoding", "identity")
        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.header("Access-Control-Allow-Headers","*")

        ctx.result(absolute.toFile().inputStream())

    }
}
