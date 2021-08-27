package ch.unibas.dmi.dbis.vrem.rest.requests

import ch.unibas.dmi.dbis.vrem.generation.model.GenerationType
import kotlinx.serialization.Serializable

@Serializable
data class GenerationRequest(
    val genType: GenerationType,
    val idList: ArrayList<String>,
    val height: Int,
    val width: Int,
    val seed: Int
    // TODO Consider adding epochs/iterations to the config.
)
