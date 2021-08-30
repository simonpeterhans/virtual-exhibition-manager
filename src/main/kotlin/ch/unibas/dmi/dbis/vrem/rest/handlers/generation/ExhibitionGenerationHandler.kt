package ch.unibas.dmi.dbis.vrem.rest.handlers.generation

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.model.GenerationType
import ch.unibas.dmi.dbis.vrem.generation.som.SomGenerator
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.rest.handlers.PostRestHandler
import ch.unibas.dmi.dbis.vrem.rest.requests.GenerationRequest
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*

class ExhibitionGenerationHandler(cineastConfig: CineastConfig) : PostRestHandler<Exhibition> {

    private val cineastHttp = CineastHttp(cineastConfig)

    override val route: String
        get() = "/generate/exhibition"

    @OpenApi(
        method = HttpMethod.POST,
        summary = "Generates a new exhibition with the specified parameters.",
        path = "/api/generate/exhibition",
        tags = ["Generation"],
        requestBody = OpenApiRequestBody(
            content = [OpenApiContent(GenerationRequest::class)],
            required = true,
            description = "The generation configuration object as JSON string."
        ),
        responses = [
            OpenApiResponse(StatusCode.OK.toString(), [OpenApiContent(ResponseMessage::class)]),
            OpenApiResponse(StatusCode.FORBIDDEN.toString(), [OpenApiContent(ResponseMessage::class)]),
            OpenApiResponse(StatusCode.BAD_REQUEST.toString(), [OpenApiContent(ResponseMessage::class)]),
        ]
    )
    override fun doPost(ctx: Context): Exhibition {
        val config = ctx.body<GenerationRequest>()

        val gen = when (config.genType) {
            GenerationType.SEMANTIC_SOM, GenerationType.VISUAL_SOM -> {
                SomGenerator(config, cineastHttp)
            }

            GenerationType.SEMANTIC_SIMILARITY -> TODO()
            GenerationType.VISUAL_SIMILARITY -> TODO()
            GenerationType.RANDOM -> TODO()
        }

        return gen.genExhibition()
    }

}
