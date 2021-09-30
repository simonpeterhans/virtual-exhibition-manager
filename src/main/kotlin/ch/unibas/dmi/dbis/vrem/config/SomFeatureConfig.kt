package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Serializable

@Serializable
class FeatureWeightPair(val tableName: String, val weight: Double)

@Serializable
class SomFeatureConfig(val featureMap: Map<String, List<FeatureWeightPair>>)
