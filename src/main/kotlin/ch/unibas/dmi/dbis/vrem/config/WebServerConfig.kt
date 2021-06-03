package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Serializable

/**
 * Configuration for the REST Endpoint
 * @property documentRoot The document root path as a string
 * @property port The port of the endpoint
 *
 * @author loris.sauter
 */
@Serializable
data class WebServerConfig(val documentRoot: String, val port: Short)
