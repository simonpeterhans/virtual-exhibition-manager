package ch.unibas.dmi.dbis.vrem.generation.model

enum class GenerationType(val cineastCategory: String = "", val tableName: String = "") {

    SEMANTIC_SOM("som_semantic", "features_visualtextcoembedding"),
    VISUAL_SOM("som_visual", "features_AverageColor"),
    SEMANTIC_SIMILARITY("sim_semantic"),
    VISUAL_SIMILARITY("sim_visual"),
    RANDOM

}
