package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Serializable

/**
 * Configuration of the REST endpoint.
 *
 * @property documentRoot The document root path as a string.
 * @property httpPort The port of the endpoint.
 */
@Serializable
data class WebServerConfig(
    val documentRoot: String,
    val httpPort: Int,
    val httpsPort: Int,
    val enableSsl: Boolean,
    val keystorePath: String,
    val keystorePass: String
)
