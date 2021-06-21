package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Serializable

/**
 * VREM configuration with database and webserver, deserialized form of config.json-like files.
 *
 * @property database The MongoDB configuration object.
 * @property server The web server configuration object (i.e., VREM document root and port).
 * @constructor
 */
@Serializable
data class Config(val database: DatabaseConfig, val server: WebServerConfig)
