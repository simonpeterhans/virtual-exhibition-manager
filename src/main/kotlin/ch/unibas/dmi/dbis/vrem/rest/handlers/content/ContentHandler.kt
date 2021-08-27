package ch.unibas.dmi.dbis.vrem.rest.handlers.content

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit.Companion.URL_ID_SUFFIX
import ch.unibas.dmi.dbis.vrem.rest.handlers.GetRestHandler
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*
import java.io.ByteArrayInputStream
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path

class ContentHandler(private val docRoot: Path, private val cineastConfig: CineastConfig) : GetRestHandler<Any> {

    companion object {

        const val PARAM_KEY_PATH = ":path"

    }

    override val route: String
        get() = "/content/${PARAM_KEY_PATH}"

    override fun doGet(ctx: Context): Any = "" // Not used, we return bytes via get() instead.

    @OpenApi(
        method = HttpMethod.GET,
        summary = "Retrieves exhibit content (e.g., an image) based on a url.",
        path = "/api/content/:path",
        pathParams = [
            OpenApiParam(
                name = "path",
                type = String::class,
                description = "The path of the exhibit's content.",
                required = true
            )
        ],
        tags = ["Content"],
        responses = [
            OpenApiResponse(StatusCode.OK.toString(), [OpenApiContent(ByteArray::class)]),
            OpenApiResponse(StatusCode.INTERNAL_SERVER_ERROR.toString(), [OpenApiContent(ResponseMessage::class)]),
        ]
    )
    override fun get(ctx: Context) {
        val path = ctx.pathParam(PARAM_KEY_PATH)

        if (path.isBlank()) {
            ctx.status(404)
            return
        }

        val res: Boolean = if (path.endsWith(URL_ID_SUFFIX)) {
            getFromRemote(ctx, path)
        } else {
            getFromLocal(ctx, path)
        }

        if (res) {
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun getFromLocal(ctx: Context, path: String): Boolean {
        val absolute = docRoot.resolve(path)

        if (!Files.exists(absolute)) {
            return false
        }

        ctx.contentType(Files.probeContentType(absolute))
        ctx.result(absolute.toFile().inputStream())

        return true
    }

    fun getFromRemote(ctx: Context, path: String): Boolean {
        // ID is composed as exhibitionID/imageID.remote.
        var resultBytes: ByteArray? = null
        val id = path.substring(path.indexOf("/") + 1, path.indexOf(URL_ID_SUFFIX))

        val (_, _, result) = cineastConfig.getCineastObjectUrlString(id).httpGet().response()

        when (result) {
            is Result.Failure -> return false
            is Result.Success -> resultBytes = result.get()
        }

        ctx.contentType(URLConnection.guessContentTypeFromStream(ByteArrayInputStream(resultBytes)))
        ctx.result(resultBytes)

        return true
    }

}
