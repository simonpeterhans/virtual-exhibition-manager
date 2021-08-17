package ch.unibas.dmi.dbis.vrem.generation

import ch.unibas.dmi.dbis.vrem.generation.som.SomExhibitionGenerator
import io.javalin.http.Context

class GenerationHandler(val cineastHttp: CineastHttp) {

    fun generate(ctx: Context): Context {
        val config = ctx.body<GenerationConfig>()

        return when (config.genType) {
            GenerationType.SEMANTIC_SOM -> {
                val somGen = SomExhibitionGenerator(config, cineastHttp)
                ctx.json(somGen.genSomEx())
            }

            GenerationType.VISUAL_SOM -> TODO()
            GenerationType.SEMANTIC_SIMILARITY -> TODO()
            GenerationType.VISUAL_SIMILARITY -> TODO()
            GenerationType.RANDOM -> TODO()
        }
    }

}
