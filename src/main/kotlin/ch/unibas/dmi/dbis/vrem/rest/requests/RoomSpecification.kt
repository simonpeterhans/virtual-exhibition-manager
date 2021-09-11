package ch.unibas.dmi.dbis.vrem.rest.requests

import kotlinx.serialization.Serializable

@Serializable
data class RoomSpecification(
    // TODO Consider adding epochs/iterations to the config.
    val height: Int, val
    width: Int
)
