package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import java.io.File

/**
 * VREM configuration with database and webserver, deserialized form of config.json-like files.
 *
 * @property database The MongoDB configuration object.
 * @property server The web server configuration object (i.e., VREM document root and port).
 * @property cineast The Cineast instance configuration object.
 * @constructor
 */
@Serializable
data class Config(val database: DatabaseConfig, val server: WebServerConfig, val cineast: CineastConfig) {

    companion object {

        private val json = Json {
            serializersModule = IdKotlinXSerializationModule
            encodeDefaults = true
        }

        /**
         * Parses the specified configuration file for the exhibition.
         *
         * @param config The name of the JSON configuration file (relative to the cwd).
         * @return The parsed configuration file.
         */
        fun readConfig(config: String): Config {
            val jsonString = File(config).readText()
            return json.decodeFromString(serializer(), jsonString)
        }

    }

}
