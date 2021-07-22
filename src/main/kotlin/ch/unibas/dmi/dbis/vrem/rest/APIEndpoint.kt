package ch.unibas.dmi.dbis.vrem.rest

import ch.unibas.dmi.dbis.vrem.cineast.client.infrastructure.ApiClient
import ch.unibas.dmi.dbis.vrem.config.Config
import ch.unibas.dmi.dbis.vrem.database.dao.VREMDao
import ch.unibas.dmi.dbis.vrem.model.api.response.ErrorResponse
import ch.unibas.dmi.dbis.vrem.rest.handlers.ExhibitHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.ExhibitionHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.RequestContentHandler
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.plugin.json.FromJsonMapper
import io.javalin.plugin.json.JavalinJson
import io.javalin.plugin.json.ToJsonMapper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import mu.KotlinLogging
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import java.io.File
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * VREM API endpoint class.
 */
@ExperimentalSerializationApi
class APIEndpoint : CliktCommand(name = "server", help = "Start the REST API endpoint") {

    private val config: String by option("-c", "--config", help = "Path to the config file").default("config.json")

    init {
        // Overwrites the default mapper (Jackson) of Javalin for serialization to make sure we're using Kotlinx.
        JavalinJson.toJsonMapper = object : ToJsonMapper {
            override fun map(obj: Any): String {
                val serializer = serializer(obj.javaClass)
                val jsonObj = Json {
                    serializersModule = IdKotlinXSerializationModule // To properly serialize IDs.
                    encodeDefaults = true // Don't omit values generated by default.
                }
                return jsonObj.encodeToString(serializer, obj)
            }
        }

        // Overwrites the default mapper (Jackson) of Javalin for deserialization to make sure we're using Kotlinx.
        JavalinJson.fromJsonMapper = object : FromJsonMapper {
            override fun <T> map(json: String, targetClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                val deserializer = serializer(targetClass) as KSerializer<T>
                val jsonObj = Json {
                    serializersModule = IdKotlinXSerializationModule // To properly deserialize IDs.
                    coerceInputValues = true // Use default values if key not provided.
                }
                return jsonObj.decodeFromString(deserializer, json)
            }
        }
    }

    override fun run() {
        val config = Config.readConfig(this.config)
        val (reader, writer) = VREMDao.getDAOs(config.database)
        val docRoot = File(config.server.documentRoot).toPath()

        // Give Cineast enough time to process the request before timing out.
        ApiClient.builder.readTimeout(Duration.ofSeconds(config.cineast.queryTimeoutSeconds))

        // Handlers.
        val exhibitionHandler = ExhibitionHandler(reader, writer)
        val contentHandler = RequestContentHandler(docRoot, config.cineast)
        val exhibitHandler = ExhibitHandler(reader, writer, docRoot)

        // API endpoint.
        val endpoint = Javalin.create { conf ->
            conf.defaultContentType = "application/json"
            conf.enableCorsForAllOrigins()

            // Logger.
            /*conf.requestLogger { ctx, ms ->
                logger.info { "Request received: ${ctx.req.requestURI}" }
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
        }

        endpoint.exception(Exception::class.java) { e, ctx ->
            logger.error(e) { "Exception occurred, sending 500 and exception name." }
            ctx.status(500)
                .json(ErrorResponse("Error of type ${e.javaClass.simpleName} occurred. Check server log for additional information."))
        }
        endpoint.after { ctx ->
            ctx.header("Access-Control-Allow-Origin", "*")
            ctx.header("Access-Control-Allow-Headers", "*")
        }
        endpoint.start(config.server.port.toInt())

        println("Started the server.")
        println("Ctrl+C to stop the server.")

        // TODO CLI to process commands (/quit and the like).
    }

}
