package ch.unibas.dmi.dbis.vrem.generation.model

enum class GenerationType(
    val featureList: ArrayList<IdDoublePair> = arrayListOf(),
    val categoryName: ArrayList<String> = arrayListOf()
) {

    // TODO Cineast categories are probably better here to avoid relying on table names.
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

    SEMANTIC_SIMILARITY( // Similarity evaluated via Cineast config.
        categoryName = arrayListOf(

        )
    ),

    VISUAL_SIMILARITY( // Similarity evaluated via Cineast config.
        categoryName = arrayListOf(

        )
    ),

    RANDOM // Nothing to specify here.

}
