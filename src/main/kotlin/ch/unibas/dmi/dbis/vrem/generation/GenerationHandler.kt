package ch.unibas.dmi.dbis.vrem.generation

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import ch.unibas.dmi.dbis.vrem.generation.som.SomGenerator
import io.javalin.http.Context

class GenerationHandler(cineastConfig: CineastConfig) {

    val cineastHttp = CineastHttp(cineastConfig)

    fun generate(ctx: Context): Context {
        val config = ctx.body<GenerationConfig>()

        // TODO Refactor this to return an exhibition or a room depending on what was requested in the config.
        return when (config.genType) {
            GenerationType.SEMANTIC_SOM -> {
                val somGen = SomGenerator(config, cineastHttp)
                ctx.json(somGen.genExhibition())
            }

            GenerationType.VISUAL_SOM -> TODO()
            GenerationType.SEMANTIC_SIMILARITY -> TODO()
            GenerationType.VISUAL_SIMILARITY -> TODO()
            GenerationType.RANDOM -> TODO()
        }
    }

}
