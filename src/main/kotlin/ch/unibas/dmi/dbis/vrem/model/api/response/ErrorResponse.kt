package ch.unibas.dmi.dbis.vrem.model.api.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val message: String)
