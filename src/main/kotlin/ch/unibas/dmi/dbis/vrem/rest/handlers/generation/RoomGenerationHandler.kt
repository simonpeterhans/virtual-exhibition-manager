package ch.unibas.dmi.dbis.vrem.rest.handlers.generation

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.model.GenerationType
import ch.unibas.dmi.dbis.vrem.generation.som.SomGenerator
import ch.unibas.dmi.dbis.vrem.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.rest.handlers.PostRestHandler
import ch.unibas.dmi.dbis.vrem.rest.requests.GenerationRequest
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode.NOT_FOUND
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode.OK
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.HttpMethod
import io.javalin.plugin.openapi.annotations.OpenApi
import io.javalin.plugin.openapi.annotations.OpenApiFormParam
import io.javalin.plugin.openapi.annotations.OpenApiResponse

class RoomGenerationHandler(cineastConfig: CineastConfig) : PostRestHandler<Room> {

    private val cineastHttp = CineastHttp(cineastConfig)

    override val route: String = "/generate/room"

    @OpenApi(
        method = HttpMethod.POST,
        summary = "Generates a new room with the specified parameters.",
        path = "/api/generate/room",
        formParams = [
            OpenApiFormParam("genType", GenerationType::class, true),
        ],
        tags = ["Generation"],
        responses = [
            OpenApiResponse(OK.toString()),
            OpenApiResponse(NOT_FOUND.toString())
        ],
        ignore = false
    )
    override fun doPost(ctx: Context): Room {
        val config = ctx.body<GenerationRequest>()

        val gen = when (config.genType) {
            GenerationType.SEMANTIC_SOM, GenerationType.VISUAL_SOM -> {
                SomGenerator(config, cineastHttp)
            }

            GenerationType.SEMANTIC_SIMILARITY -> TODO()
            GenerationType.VISUAL_SIMILARITY -> TODO()
            GenerationType.RANDOM -> TODO()
        }

        return gen.genRoom()
    }

}
