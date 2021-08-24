package ch.unibas.dmi.dbis.vrem.generation.model

enum class GenerationType(
    val cineastCategory: String = "",
    val featureList: ArrayList<Pair<String, Double>> = arrayListOf()
) {

    // TODO Decide on how to load features (Cineast category or list of table names).
    // TODO Allow storage of SOM features to use via config (similarity should be defined in the Cineast config).
    SEMANTIC_SOM(
        "som_semantic",
        arrayListOf(Pair("features_visualtextcoembedding", 1.0))
    ),

    VISUAL_SOM(
        "som_visual",
        arrayListOf(
            Pair("features_AverageColor", 2.3),
            Pair("features_AverageColorARP44", 0.5),
//            Pair("features_AverageColorCLD", 1.3),
            Pair("features_AverageColorGrid8", 1.8),
//            Pair("features_AverageColorGrid8Reduced15", 1.0),
            Pair("features_AverageFuzzyHist", 0.7),
            Pair("features_CLD", 1.3),
//            Pair("features_EdgeARP88", 1.0),
            Pair("features_EdgeGrid16", 1.4),
            Pair("features_EHD", 0.7),
            Pair("features_MedianColor", 1.2),
            Pair("features_MedianColorGrid8", 1.7),
            Pair("features_hogmf25k512", 1.0),
            Pair("features_surfmf25k512", 1.0)
        )
    ),

    SEMANTIC_SIMILARITY("sim_semantic"),

    VISUAL_SIMILARITY("sim_visual"),

    RANDOM

}
