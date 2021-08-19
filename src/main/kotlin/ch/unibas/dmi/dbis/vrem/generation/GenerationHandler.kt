package ch.unibas.dmi.dbis.vrem.generation

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import ch.unibas.dmi.dbis.vrem.generation.model.GenerationConfig
import ch.unibas.dmi.dbis.vrem.generation.model.GenerationObject
import ch.unibas.dmi.dbis.vrem.generation.model.GenerationType
import ch.unibas.dmi.dbis.vrem.generation.som.SomGenerator
import io.javalin.http.Context

class GenerationHandler(cineastConfig: CineastConfig) {

    private val cineastHttp = CineastHttp(cineastConfig)

    fun generate(ctx: Context): Context {
        val config = ctx.body<GenerationConfig>()

        // TODO Refactor this to return an exhibition or a room depending on what was requested in the config.
        val gen = when (config.genType) {
            GenerationType.SEMANTIC_SOM -> {
                SomGenerator(config, cineastHttp)
            }

            GenerationType.VISUAL_SOM -> TODO()
            GenerationType.SEMANTIC_SIMILARITY -> TODO()
            GenerationType.VISUAL_SIMILARITY -> TODO()
            GenerationType.RANDOM -> TODO()
        }

        return when (config.genObj) {
            GenerationObject.EXHIBITION -> ctx.json(gen.genExhibition())
            GenerationObject.ROOM -> ctx.json(gen.genRoom())
        }
    }

}
