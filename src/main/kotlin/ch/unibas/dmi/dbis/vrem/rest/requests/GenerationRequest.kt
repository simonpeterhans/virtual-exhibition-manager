package ch.unibas.dmi.dbis.vrem.rest.requests

import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.Generator
import ch.unibas.dmi.dbis.vrem.generation.model.GenerationType
import ch.unibas.dmi.dbis.vrem.generation.random.RandomGenerator
import ch.unibas.dmi.dbis.vrem.generation.som.SomGenerator
import kotlinx.serialization.Serializable

@Serializable
data class GenerationRequest(
    val genType: GenerationType,
    val idList: ArrayList<String>,
    val height: Int,
    val width: Int,
    val seed: Int
    // TODO Consider adding epochs/iterations to the config.
) {

    companion object {

        fun getGenerator(config: GenerationRequest, cineastHttp: CineastHttp): Generator {
            return when (config.genType) {
                GenerationType.SEMANTIC_SOM, GenerationType.VISUAL_SOM -> {
                    SomGenerator(config, cineastHttp)
                }

                GenerationType.SEMANTIC_SIMILARITY, GenerationType.VISUAL_SIMILARITY -> TODO()

                GenerationType.RANDOM -> {
                    RandomGenerator(config, cineastHttp)
                }
            }
        }

    }

}
