package ch.unibas.dmi.dbis.vrem.kotlin.model.api.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val message: String)
