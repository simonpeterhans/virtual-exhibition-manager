package ch.unibas.dmi.dbis.vrem.generation

import ch.unibas.dmi.dbis.vrem.cineast.client.apis.MetadataApi
import ch.unibas.dmi.dbis.vrem.cineast.client.apis.ObjectApi
import ch.unibas.dmi.dbis.vrem.cineast.client.models.AllFeaturesByCategoryQueryResult
import ch.unibas.dmi.dbis.vrem.generation.model.DoubleFeatureData

object CineastRest {

    private const val SEGMENT_SUFFIX = "_1"
    private const val CINEAST_FEATURE_LABEL = "feature"
    private const val CINEAST_ID_LABEL = "id"

    fun getAllIds(): List<String> {
        val ids = ObjectApi().findObjectsAll()

        if (ids.content == null) {
            return arrayListOf()
        }

        return ids.content.map { o -> o.objectId + SEGMENT_SUFFIX }.toCollection(ArrayList())
    }

    fun getFeaturesByCategory(category: String): Map<String, List<Map<String, Any>>> {
        val features: AllFeaturesByCategoryQueryResult = MetadataApi().findAllFeatByCat(category)

        if (features.featureMap == null) {
            return mapOf()
        }

        return features.featureMap
    }

    fun featureListToFeatureData(featureName: String, featureList: List<Map<String, Any>>): DoubleFeatureData {
        val featureData = DoubleFeatureData(featureName)

        for (e in featureList) {
            @Suppress("UNCHECKED_CAST")
            featureData.addSample(
                (e[CINEAST_ID_LABEL] as String).substringBeforeLast(SEGMENT_SUFFIX),
                e[CINEAST_FEATURE_LABEL] as ArrayList<Double>
            )
        }

        return featureData
    }

    fun getFeatureDataFromCategory(category: String): MutableMap<String, DoubleFeatureData> {
        val allFeatures = getFeaturesByCategory(category)

        val featureDataList = mutableMapOf<String, DoubleFeatureData>()

        for (e in allFeatures.entries) {
            featureDataList[e.key] = featureListToFeatureData(e.key, e.value)
        }

        return featureDataList
    }

}
