package ch.unibas.dmi.dbis.vrem.rest.requests

import ch.unibas.dmi.dbis.vrem.generation.model.SomGenType
import kotlinx.serialization.Serializable

@Serializable
class SomGenerationRequest(
    val roomSpec: RoomSpecification,
    val genType: SomGenType,
    val idList: ArrayList<String>,
    val seed: Int
)
