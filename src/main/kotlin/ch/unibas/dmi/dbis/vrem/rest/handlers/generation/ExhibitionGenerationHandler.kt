package ch.unibas.dmi.dbis.vrem.rest.handlers.generation

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.rest.handlers.PostRestHandler
import ch.unibas.dmi.dbis.vrem.rest.requests.GenerationRequest
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*

class ExhibitionGenerationHandler(private val cineastConfig: CineastConfig) : PostRestHandler<Exhibition> {

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
            OpenApiResponse(StatusCode.OK.toString(), [OpenApiContent(Exhibition::class)]),
            OpenApiResponse(StatusCode.FORBIDDEN.toString(), [OpenApiContent(ResponseMessage::class)]),
            OpenApiResponse(StatusCode.BAD_REQUEST.toString(), [OpenApiContent(ResponseMessage::class)]),
        ]
    )
    override fun doPost(ctx: Context): Exhibition {
        val config = ctx.body<GenerationRequest>()

        return GenerationRequest.getGenerator(cineastConfig, config, cineastHttp).genExhibition()
    }

}
