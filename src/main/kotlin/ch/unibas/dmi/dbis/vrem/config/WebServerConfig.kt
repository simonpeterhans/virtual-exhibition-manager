package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Serializable

/**
 * Configuration of the REST endpoint.
 *
 * @property documentRoot The document root path as a string.
 * @property port The port of the endpoint.
 * @property enableSsl if ssl / https should be enabled
 * @property keystorePassword password for the keystore
 * @property keystorePath path for the keystore
 */
@Serializable
data class WebServerConfig(val documentRoot: String, val port: Int, val enableSsl: Boolean, val keystorePath: String, val keystorePassword: String)
