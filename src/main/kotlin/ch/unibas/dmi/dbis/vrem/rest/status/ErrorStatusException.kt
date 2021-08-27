package ch.unibas.dmi.dbis.vrem.rest.status

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class ErrorStatusException(
    val statusCode: Int,
    val status: String,
    val log: Boolean = true
) : Exception(status) {

    init {
        if (log) {
            logger.info {
                "ErrorStatusException (code: $statusCode, message: $status) thrown by ${stackTrace.first()}."
            }
        }
    }

}
