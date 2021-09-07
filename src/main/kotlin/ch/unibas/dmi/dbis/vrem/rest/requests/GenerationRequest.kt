package ch.unibas.dmi.dbis.vrem.rest.requests

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.Generator
import ch.unibas.dmi.dbis.vrem.generation.model.GenerationType
import ch.unibas.dmi.dbis.vrem.generation.random.RandomGenerator
import ch.unibas.dmi.dbis.vrem.generation.similarity.SimilarityGenerator
import ch.unibas.dmi.dbis.vrem.generation.som.SomGenerator
import kotlinx.serialization.Serializable

@Serializable
data class GenerationRequest(
    val genType: GenerationType,
    val idList: ArrayList<String>, // First slot contains the ID of the image/exhibit the request was issued from.
    val height: Int,
    val width: Int,
    val seed: Int
    // TODO Consider adding epochs/iterations to the config.
) {

    companion object {

        fun getGenerator(cineastConf: CineastConfig, genConf: GenerationRequest, cineastHttp: CineastHttp): Generator {
            return when (genConf.genType) {
                GenerationType.SEMANTIC_SOM, GenerationType.VISUAL_SOM -> {
                    SomGenerator(genConf, cineastHttp)
                }

                GenerationType.SEMANTIC_SIMILARITY, GenerationType.VISUAL_SIMILARITY -> {
                    SimilarityGenerator(cineastConf, genConf, cineastHttp)
                }

                GenerationType.RANDOM -> {
                    RandomGenerator(genConf, cineastHttp)
                }
            }
        }

    }

}
