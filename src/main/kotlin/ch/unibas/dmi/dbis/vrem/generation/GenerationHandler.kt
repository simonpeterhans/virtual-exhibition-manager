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

        val gen = when (config.genType) {
            GenerationType.SEMANTIC_SOM, GenerationType.VISUAL_SOM -> {
                SomGenerator(config, cineastHttp)
            }

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
