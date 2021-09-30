package ch.unibas.dmi.dbis.vrem.rest.requests

import kotlinx.serialization.Serializable

@Serializable
data class SomGenerationRequest(
    val roomSpec: RoomSpecification,
    val genType: String,
    val idList: ArrayList<String>,
    val seed: Int,
    val numEpochs: Int
)
