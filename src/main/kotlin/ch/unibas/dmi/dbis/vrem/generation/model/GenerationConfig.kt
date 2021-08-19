package ch.unibas.dmi.dbis.vrem.generation.model

import kotlinx.serialization.Serializable


@Serializable
data class GenerationConfig(
    val genObj: GenerationObject,
    val genType: GenerationType,
    val idList: ArrayList<String>,
    val height: Int,
    val width: Int,
    val seed: Int
    // TODO Consider adding epochs/iterations to the config.
)
