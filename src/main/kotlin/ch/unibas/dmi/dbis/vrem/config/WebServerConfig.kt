package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Serializable

/**
 * Configuration of the REST endpoint.
 *
 * @property documentRoot The document root path as a string.
 * @property port The port of the endpoint.
 * @constructor
 */
@Serializable
data class WebServerConfig(val documentRoot: String, val port: Short)
