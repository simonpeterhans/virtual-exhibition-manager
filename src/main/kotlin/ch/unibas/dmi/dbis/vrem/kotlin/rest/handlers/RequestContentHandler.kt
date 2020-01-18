package ch.unibas.dmi.dbis.vrem.kotlin.rest.handlers

import io.javalin.http.Context
import kotlinx.io.InputStream
import kotlinx.io.OutputStream
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path

class RequestContentHandler(private val docRoot: Path) {

    private val LOGGER = LogManager.getLogger(RequestContentHandler::class.java)

    companion object {
        const val PARAM_KEY_PATH = ":path"

        fun fastCopy(src:InputStream, dest:OutputStream): Int {
            val buffer = ByteArray(1024) // Adjustable, config?
            var read = 0;
            var totalRead = 0;
            while (read != -1){
                read = src.read(buffer)
                dest.write(buffer, 0, read)
                totalRead += read
            }
            return totalRead
        }
    }

    fun serveContent(ctx: Context){
        val path = ctx.pathParam(PARAM_KEY_PATH)

        if(path.isBlank()){
            ctx.status(404)
            return
        }

        val absolute = docRoot.resolve(path)
        if(!Files.exists(absolute)){
            LOGGER.debug("Cannot serve $absolute, as it does not exist")
            ctx.status(404)
        }

        ctx.contentType(Files.probeContentType(absolute))
        ctx.header("Transfer-Encoding", "identity")
        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.header("Access-Control-Allow-Headers","*")

        ctx.result(absolute.toFile().inputStream())

    }
}
