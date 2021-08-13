package ch.unibas.dmi.dbis.vrem.generation

import kotlinx.serialization.Serializable

enum class GenerationType(val cineastCategory: String = "", val tableName: String = "") {
    SEMANTIC_SOM("som_semantic", "features_visualtextcoembedding"), // Only contains 1 feature.
    VISUAL_SOM("som_visual", "features_AverageColor"),
    SEMANTIC_SIMILARITY("sim_semantic"),
    VISUAL_SIMILARITY("sim_visual"),
    RANDOM
}

@Serializable
data class GenerationConfig(
    val genType: GenerationType,
    val idList: ArrayList<String>,
    val height: Int,
    val width: Int,
    val seed: Int
    // TODO Consider adding epochs/iterations to the config.
)
