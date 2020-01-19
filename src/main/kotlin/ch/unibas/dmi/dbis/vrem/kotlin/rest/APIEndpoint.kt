package ch.unibas.dmi.dbis.vrem.kotlin.rest

import ch.unibas.dmi.dbis.vrem.kotlin.config.Config
import ch.unibas.dmi.dbis.vrem.kotlin.config.DatabaseConfig
import ch.unibas.dmi.dbis.vrem.kotlin.database.dao.VREMReader
import ch.unibas.dmi.dbis.vrem.kotlin.database.dao.VREMWriter
import ch.unibas.dmi.dbis.vrem.kotlin.model.api.response.ErrorResponse
import ch.unibas.dmi.dbis.vrem.kotlin.rest.handlers.ExhibitHandler
import ch.unibas.dmi.dbis.vrem.kotlin.rest.handlers.ExhibitionHandler
import ch.unibas.dmi.dbis.vrem.kotlin.rest.handlers.RequestContentHandler
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.plugin.json.FromJsonMapper
import io.javalin.plugin.json.JavalinJson
import io.javalin.plugin.json.ToJsonMapper
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.litote.kmongo.KMongo
import org.litote.kmongo.json
import java.io.File
import java.nio.file.Files

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class APIEndpoint : CliktCommand(name = "server", help = "Start the REST API endpoint") {

    val config: String by option("-c", "--config", help = "Path to the config file").default("config.json")

    private val LOGGER = LogManager.getLogger(APIEndpoint::class.java)

    init {
        JavalinJson.toJsonMapper = object : ToJsonMapper {
            override fun map(obj: Any):String{
                return obj.json
            }
        }
        JavalinJson.fromJsonMapper = object : FromJsonMapper {
            override fun <T> map(jsonString:String, targetClass:Class<T>):T{
                return targetClass.newInstance()
            }
        }
    }

    override fun run() {
        val config = readConfig()
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
                    get { exhibitionHandler.listExhibitions(it) }
                }
                path("load/:id") {
                    get { exhibitionHandler.loadExhibitionById(it) }
                }
                path("loadbyname/:name") {
                    get { exhibitionHandler.loadExhibitionByName(it) }
                }
                post("save") { exhibitionHandler.saveExhibition(it) }
            }
            path("/content/get/:path") {
                get { contentHandler.serveContent(it) }
            }
            path("/exhibits") {
                path("list") {
                    get { exhibitHandler.listExhibits(it) }
                }
                post("upload") { exhibitHandler.saveExhibit(it) }
            }
        }
        // Exception Handling, semi-transparent
        endpoint.exception(Exception::class.java){e,ctx ->
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

    private fun getDAOs(dbConfig: DatabaseConfig): Pair<VREMReader, VREMWriter> {
        //val registry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(VREMCodecProvider()))
        //val dbSettings = MongoClientSettings.builder().codecRegistry(registry).applyConnectionString(dbConfig.getConnectionString()).applicationName("VREM").build()
        //val dbClient = MongoClients.create(dbSettings)
        val dbClient = KMongo.createClient(dbConfig.getConnectionString())
        val db = dbClient.getDatabase(dbConfig.database)
        return VREMReader(db) to VREMWriter(db)
    }

    private fun readConfig(): Config {
        val jsonString = File(this.config).readText()
        return Json(kotlinx.serialization.json.JsonConfiguration.Stable).parse(Config.serializer(), jsonString)
    }
}