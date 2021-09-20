package ch.unibas.dmi.dbis.vrem.rest.requests

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
data class RoomSpecification(
    var height: Int,
    var width: Int
) {

    @Schema(hidden = true)
    fun getTotalElements(): Int = height * width

}
