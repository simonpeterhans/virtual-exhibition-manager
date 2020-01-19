package ch.unibas.dmi.dbis.vrem.kotlin.config

import kotlinx.serialization.Serializable

/**
 * VREM configuration.
 *
 * @property database Database config
 * @property server Endpoint config
 *
 * @author loris.sauter
 */
@Serializable
data class Config (val database:DatabaseConfig, val server:WebServerConfig)