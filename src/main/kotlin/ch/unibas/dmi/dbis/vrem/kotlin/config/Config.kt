package ch.unibas.dmi.dbis.vrem.kotlin.config

/**
 * VREM configuration.
 *
 * @property database Database config
 * @property server Endpoint config
 *
 * @author loris.sauter
 */
data class Config (val database:DatabaseConfig, val server:WebServerConfig)