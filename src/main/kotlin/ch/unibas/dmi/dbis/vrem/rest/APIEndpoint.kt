package ch.unibas.dmi.dbis.vrem.rest

import ch.unibas.dmi.dbis.vrem.config.Config
import ch.unibas.dmi.dbis.vrem.config.DatabaseConfig
import ch.unibas.dmi.dbis.vrem.database.dao.VREMReader
import ch.unibas.dmi.dbis.vrem.database.dao.VREMWriter
import ch.unibas.dmi.dbis.vrem.model.api.response.ErrorResponse
import ch.unibas.dmi.dbis.vrem.rest.handlers.ExhibitHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.ExhibitionHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.RequestContentHandler
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.plugin.json.JavalinJackson
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.KMongo
import org.litote.kmongo.id.toId
import java.io.File
import java.nio.file.Files

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class APIEndpoint : CliktCommand(name = "server", help = "Start the REST API endpoint") {

    val config: String by option("-c", "--config", help = "Path to the config file").default("config.json")

    private val LOGGER = LogManager.getLogger(APIEndpoint::class.java)

    companion object {
        val json = Json(kotlinx.serialization.json.JsonConfiguration.Stable)

        val idSerializer = object : JsonSerializer<Id<Any>>() {
            override fun serialize(value: Id<Any>?, gen: JsonGenerator?, serializers: SerializerProvider?) {
                gen?.writeString(value.toString())
            }

            override fun handledType(): Class<Id<Any>> {
                return Id::class.java as Class<Id<Any>>
            }
        }
        val idDeserializer = object : JsonDeserializer<Id<Any>>() {
            override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Id<Any> {
                val string = p?.valueAsString!!
                return ObjectId(string).toId()
            }

        }

        fun getDAOs(dbConfig: DatabaseConfig): Pair<VREMReader, VREMWriter> {
            val dbClient = KMongo.createClient(dbConfig.getConnectionString())
            val db = dbClient.getDatabase(dbConfig.database)
            return VREMReader(db) to VREMWriter(db)
        }

        fun readConfig(config: String): Config {
            val jsonString = File(config).readText()
            return json.parse(Config.serializer(), jsonString)
        }
    }

    init {
        JavalinJackson.getObjectMapper().registerModule(SimpleModule("KMongoSupport").addSerializer(idSerializer).addDeserializer(Id::class.java, idDeserializer))
    }

    override fun run() {
        val config = readConfig(this.config)
        val (reader, writer) = getDAOs(config.database)

        val docRoot = File(config.server.documentRoot).toPath()
        if (!Files.exists(docRoot)) {
            throw IOException("DocumentRoot ${docRoot} does not exist")
        }

        /* Handlers */
        val exhibitionHandler = ExhibitionHandler(reader, writer)
        val contentHandler = RequestContentHandler(docRoot)
        val exhibitHandler = ExhibitHandler(reader, writer, docRoot)

        /* API Endpoint */
        val endpoint = Javalin.create { config ->
            config.defaultContentType = "application/json"
            config.enableCorsForAllOrigins()
        }.routes {
            path("/exhibitions") {
                path("list") {
                    get ( exhibitionHandler::listExhibitions )
                }
                path("load/:id") {
                    get ( exhibitionHandler::loadExhibitionById )
                }
                path("loadbyname/:name") {
                    get ( exhibitionHandler::loadExhibitionByName )
                }
                path("save"){
                  post(exhibitionHandler::saveExhibition)
                }
            }
            path("/content/get/:path") {
                get ( contentHandler::serveContent )
            }
            path("/exhibits") {
                path("list") {
                    get ( exhibitHandler::listExhibits )
                }
                path("upload"){
                    post(exhibitHandler::saveExhibit)
                }
            }
        }
        // Exception Handling, semi-transparent
        endpoint.exception(Exception::class.java) { e, ctx ->
            LOGGER.error("An exception occurred. Sending 500 and exception name", e)
            ctx.status(500).json(ErrorResponse("Error of type ${e.javaClass.simpleName} occurred. See the server log for more info"))
        }
        endpoint.after { ctx ->
            ctx.header("Access-Control-Allow-Origin", "*")
            ctx.header("Access-Control-Allow-Headers", "*")
        }
        endpoint.start(config.server.port.toInt())
        println("Started the server...")
        println("Ctrl+C to stop the server")
        // TODO make CLI-alike to gracefully stop the server
    }


}