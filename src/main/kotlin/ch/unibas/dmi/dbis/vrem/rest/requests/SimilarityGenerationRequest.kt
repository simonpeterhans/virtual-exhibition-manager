package ch.unibas.dmi.dbis.vrem.rest.requests

import kotlinx.serialization.Serializable

@Serializable
data class SimilarityGenerationRequest(
    val roomSpec: RoomSpecification,
    val genType: String,
    val objectId: String
)
