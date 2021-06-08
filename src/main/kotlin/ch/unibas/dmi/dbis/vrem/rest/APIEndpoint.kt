package ch.unibas.dmi.dbis.vrem.rest

import ch.unibas.dmi.dbis.vrem.config.Config
import ch.unibas.dmi.dbis.vrem.config.DatabaseConfig
import ch.unibas.dmi.dbis.vrem.database.dao.VREMReader
import ch.unibas.dmi.dbis.vrem.database.dao.VREMWriter
import ch.unibas.dmi.dbis.vrem.generate.CollectionGenerator
import ch.unibas.dmi.dbis.vrem.model.api.response.ErrorResponse
import ch.unibas.dmi.dbis.vrem.rest.handlers.ExhibitHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.ExhibitionHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.RequestContentHandler
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.litote.kmongo.KMongo
import java.io.File
import java.io.IOException
import java.nio.file.Files

/**
 * VREM API endpoint class.
 *
 * @constructor
 */
class APIEndpoint : CliktCommand(name = "server", help = "Start the REST API endpoint") {

    private val config: String by option("-c", "--config", help = "Path to the config file").default("config.json")

    companion object {
        private val LOGGER = LogManager.getLogger(APIEndpoint::class.java)

        /**
         * Static method to create data access objects (reader & writer) to access the exhibition/exhibit collections.
         *
         * @param dbConfig The database configuration for the MongoDB instance.
         * @return A pair of VREMReader and VREMWriter to access the database.
         */
        fun getDAOs(dbConfig: DatabaseConfig): Pair<VREMReader, VREMWriter> {
            val dbClient = KMongo.createClient(dbConfig.getConnectionString())
            val db = dbClient.getDatabase(dbConfig.database)
            return VREMReader(db) to VREMWriter(db)
        }

        /**
         * Parses the specified configuration file for the exhibition.
         *
         * @param config The name of the JSON configuration file (relative to the cwd).
         * @return The parsed configuration file.
         */
        fun readConfig(config: String): Config {
            val jsonString = File(config).readText()
            return Json.decodeFromString(Config.serializer(), jsonString)
        }
    }

    override fun run() {
        val config = readConfig(this.config)
        val (reader, writer) = getDAOs(config.database)

        val docRoot = File(config.server.documentRoot).toPath()
        if (!Files.exists(docRoot)) {
            throw IOException("DocumentRoot $docRoot does not exist!")
        }

        // Handlers.
        val exhibitionHandler = ExhibitionHandler(reader, writer)
        val contentHandler = RequestContentHandler(docRoot)
        val exhibitHandler = ExhibitHandler(reader, writer, docRoot)

        // Collection generator.
        val collectionGenerator = CollectionGenerator()
//        val collectionGenerator = CollectionGenerator(docRoot, writer)

        // API endpoint.
        val endpoint = Javalin.create { conf ->
            conf.defaultContentType = "application/json"
            conf.enableCorsForAllOrigins()

            // Logger.
            /*conf.requestLogger { ctx, ms ->
                LOGGER.info("Request received: ${ctx.req.requestURI}")
            }*/
        }.routes {
            path("/exhibitions") {
                path("list") {
                    get(exhibitionHandler::listExhibitions)
                }
                path("load/:id") {
                    get(exhibitionHandler::loadExhibitionById)
                }
                path("loadbyname/:name") {
                    get(exhibitionHandler::loadExhibitionByName)
                }
                path("save") {
                    post(exhibitionHandler::saveExhibition)
                }
            }
            path("/content/get/:path") {
                get(contentHandler::serveContent)
            }
            path("/exhibits") {
                path("list") {
                    get(exhibitHandler::listExhibits)
                }
                path("upload") {
                    post(exhibitHandler::saveExhibit)
                }
            }
            path("/generate") {
                get(collectionGenerator::generateRandomExhibitionForContext)
            }
        }

        // Exception Handling, semi-transparent.
        endpoint.exception(Exception::class.java) { e, ctx ->
            LOGGER.error("An exception occurred. Sending 500 and exception name.", e)
            ctx.status(500)
                .json(ErrorResponse("Error of type ${e.javaClass.simpleName} occurred. See the server log for more info."))
        }
        endpoint.after { ctx ->
            ctx.header("Access-Control-Allow-Origin", "*")
            ctx.header("Access-Control-Allow-Headers", "*")
        }
        endpoint.start(config.server.port.toInt())

        println("Started the server.")
        println("Ctrl+C to stop the server.")

        // TODO Make this CLI-alike to gracefully stop the server.
    }

}
