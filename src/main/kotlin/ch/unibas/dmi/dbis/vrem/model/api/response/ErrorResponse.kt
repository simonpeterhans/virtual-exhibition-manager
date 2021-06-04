package ch.unibas.dmi.dbis.vrem.model.api.response

import kotlinx.serialization.Serializable

/**
 * Error response object.
 *
 * @property message The error message as a string.
 * @constructor
 */
@Serializable
data class ErrorResponse(val message: String)
