package ch.unibas.dmi.dbis.vrem.rest.requests

import kotlinx.serialization.Serializable

@Serializable
data class RandomGenerationRequest(
    val roomSpec: RoomSpecification,
    val idList: ArrayList<String>,
    val seed: Int
)
