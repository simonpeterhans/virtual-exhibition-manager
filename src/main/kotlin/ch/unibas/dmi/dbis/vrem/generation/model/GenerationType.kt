package ch.unibas.dmi.dbis.vrem.generation.model

enum class GenerationType(
    val featureList: ArrayList<IdDoublePair> = arrayListOf()
) {

    // TODO Decide on how to load features (Cineast category or list of table names).
    // TODO Allow storage of SOM features to use via config (similarity features should be defined in the Cineast config).
    SEMANTIC_SOM(
        arrayListOf(IdDoublePair("features_visualtextcoembedding", 1.0))
    ),

    VISUAL_SOM(
        arrayListOf(
            IdDoublePair("features_AverageColor", 2.3),
            IdDoublePair("features_AverageColorARP44", 0.5),
//            IdDoublePair("features_AverageColorCLD", 1.3),
//            IdDoublePair("features_AverageColorGrid8", 1.8),
//            IdDoublePair("features_AverageColorGrid8Reduced15", 1.0),
//            IdDoublePair("features_AverageFuzzyHist", 0.7),
//            IdDoublePair("features_CLD", 1.3),
//            IdDoublePair("features_EdgeARP88", 1.0),
//            IdDoublePair("features_EdgeGrid16", 1.4),
//            IdDoublePair("features_EHD", 0.7),
//            IdDoublePair("features_MedianColor", 1.2),
//            IdDoublePair("features_MedianColorGrid8", 1.7),
//            IdDoublePair("features_hogmf25k512", 1.0),
//            IdDoublePair("features_surfmf25k512", 1.0)
        )
    ),

    SEMANTIC_SIMILARITY,

    VISUAL_SIMILARITY,

    RANDOM

}
