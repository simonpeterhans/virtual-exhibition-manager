package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Serializable


/**
 * Configuration of the Cineast instance to serve content from.
 *
 * @property host The host address of the Cineast instance.
 * @property port The port of the Cineast instance.
 * @property objectPath The path to serve objects from (example: "/objects" for http://host:port/objects/your_obj_id).
 * @property queryTimeoutSeconds The duration to wait for a query result from Cineast after a request.
 * @property somFeatures A SOM feature config, specifying the table names of the Cottontail DB features to use.
 * @property simCategories A mapping of category names to use for VREP to category names defined in the Cineast config for similarity search.
 */
@Suppress("HttpUrlsUsage")
@Serializable
data class CineastConfig(
    val host: String,
    val port: Int,
    val objectPath: String,
    val queryTimeoutSeconds: Long,
    val somFeatures: SomFeatureConfig,
    val simCategories: Map<String, String>
) {

    private val cineastUrl = "$host:$port"
    private val cineastObjectUrl = "$cineastUrl$objectPath"

    fun getCineastObjectUrlString(objectId: String): String {
        return "$cineastObjectUrl/$objectId"
    }

}
