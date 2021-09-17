package ch.unibas.dmi.dbis.vrem.rest.requests

import ch.unibas.dmi.dbis.vrem.generation.model.SimGenType
import kotlinx.serialization.Serializable

@Serializable
data class SimilarityGenerationRequest(
    val roomSpec: RoomSpecification,
    val genType: SimGenType,
    val objectId: String
)
