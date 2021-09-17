package ch.unibas.dmi.dbis.vrem.generation.model

enum class SomGenType(
    val featureList: ArrayList<IdDoublePair> = arrayListOf()
) {

    SEMANTIC(
        arrayListOf(IdDoublePair("features_visualtextcoembedding", 1.0))
    ),

    VISUAL(
        arrayListOf(
//            IdDoublePair("features_AverageColor", 1.0),
//            IdDoublePair("features_AverageColorARP44", 1.0),
//            IdDoublePair("features_AverageColorCLD", 1.0),
//            IdDoublePair("features_AverageColorGrid8", 1.0),
//            IdDoublePair("features_AverageColorGrid8Reduced15", 1.0),
//            IdDoublePair("features_AverageFuzzyHist", 1.0),
//            IdDoublePair("features_CLD", 1.0),
//            IdDoublePair("features_EdgeARP88", 1.0),
//            IdDoublePair("features_EdgeGrid16", 1.0),
//            IdDoublePair("features_EHD", 1.0),
//            IdDoublePair("features_MedianColor", 1.0),
            IdDoublePair("features_MedianColorGrid8", 1.0),
//            IdDoublePair("features_hogmf25k512", 1.0),
//            IdDoublePair("features_surfmf25k512", 1.0)
        )
    )

}
