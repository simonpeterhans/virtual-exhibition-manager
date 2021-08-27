package ch.unibas.dmi.dbis.vrem.extensions

import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.ErrorStatusException
import io.javalin.http.Context

fun Context.errorResponse(status: Int, errorMessage: String) {
    this.status(status)
    this.json(ResponseMessage(errorMessage))
}

fun Context.errorResponse(error: ErrorStatusException) {
    this.status(error.statusCode)
    this.json(ResponseMessage(error.status))
}
