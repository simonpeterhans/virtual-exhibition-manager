package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Configuration of the Cineast instance to serve content from.
 *
 * @property host The host address of the Cineast instance.
 * @property port The port of the Cineast instance.
 * @property objectPath The path to serve objects from (example: "/objects" for http://localhost:4567/objects/your_obj_id).
 * @property queryTimeoutSeconds The duration to wait for a query result from Cineast after a request.
 * @constructor
 */
@Suppress("HttpUrlsUsage")
@Serializable
data class CineastConfig(
    val host: String,
    val port: Int,
    val objectPath: String,
    val queryTimeoutSeconds: Long
) {

    @Contextual
    val cineastUrl = "http://$host:$port"

    @Contextual
    val cineastObjectUrl = "$cineastUrl$objectPath"

    fun getCineastObjectUrlString(objectId: String): String {
        return "$cineastObjectUrl/$objectId"
    }

}
