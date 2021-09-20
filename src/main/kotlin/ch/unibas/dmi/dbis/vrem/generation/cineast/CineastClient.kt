package ch.unibas.dmi.dbis.vrem.generation.cineast

import ch.unibas.dmi.dbis.vrem.cineast.client.apis.MetadataApi
import ch.unibas.dmi.dbis.vrem.cineast.client.apis.ObjectApi
import ch.unibas.dmi.dbis.vrem.cineast.client.models.FeaturesByCategoryQueryResult
import ch.unibas.dmi.dbis.vrem.cineast.client.models.FeaturesByTableNameQueryResult
import ch.unibas.dmi.dbis.vrem.generation.model.DoubleFeatureData

object CineastClient {

    const val SEGMENT_SUFFIX = "_1"
    const val CINEAST_FEATURE_LABEL = "feature"
    const val CINEAST_ID_LABEL = "id"

    fun cleanId(id: String): String {
        return id.substringBeforeLast(SEGMENT_SUFFIX)
    }

    fun getAllIds(): List<String> {
        val ids = ObjectApi().findObjectsAll()

        if (ids.content == null) {
            return arrayListOf()
        }

        return ids.content.map { o -> o.objectId + SEGMENT_SUFFIX }.toCollection(ArrayList())
    }

    fun getFeaturesByCategory(category: String, idList: List<String>): Map<String, List<Map<String, Any>>> {
        val features: FeaturesByCategoryQueryResult = MetadataApi().findFeaturesByCategory(category, idList)

        if (features.featureMap == null) {
            return mapOf()
        }

        return features.featureMap
    }

    fun getFeatureDataByTableName(tableName: String, idList: List<String>): List<Map<String, Any>> {
        val feature: FeaturesByTableNameQueryResult = MetadataApi().findFeaturesByTableName(tableName, idList)

        if (feature.featureMap == null) {
            return listOf()
        }

        return feature.featureMap
    }

    fun featureListToFeatureData(featureName: String, featureList: List<Map<String, Any>>): DoubleFeatureData {
        val featureData = DoubleFeatureData(featureName)

        for (e in featureList) {
            @Suppress("UNCHECKED_CAST")
            featureData.addSample(
                (e[CINEAST_ID_LABEL] as String),
                e[CINEAST_FEATURE_LABEL] as ArrayList<Double>
            )
        }

        return featureData
    }

    fun getFeatureDataFromCategory(category: String, idList: List<String>): MutableMap<String, DoubleFeatureData> {
        val allFeatures = getFeaturesByCategory(category, idList)

        val featureDataList = mutableMapOf<String, DoubleFeatureData>()

        for (e in allFeatures.entries) {
            featureDataList[e.key] = featureListToFeatureData(e.key, e.value)
        }

        return featureDataList
    }

    fun getFeatureDataFromTableName(tableName: String, idList: List<String>): DoubleFeatureData {
        return featureListToFeatureData(tableName, getFeatureDataByTableName(tableName, idList))
    }

}
