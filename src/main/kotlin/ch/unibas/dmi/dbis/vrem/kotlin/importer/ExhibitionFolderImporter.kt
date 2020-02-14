package ch.unibas.dmi.dbis.vrem.kotlin.importer

import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.kotlin.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.kotlin.rest.APIEndpoint
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Path
import kotlin.system.exitProcess

class ExhibitionFolderImporter : CliktCommand(name="import-folder", help="Imports a folder-based exhibition"){

    private val LOGGER = LogManager.getLogger(ExhibitionFolderImporter::class.java)

    val exhibitionPath by option("-p", "--path", help = "Path to the exhibition root folder").required()
    val config by option("-c", "--config", help="Relative of full path to the config file to be used").required()
    val clean by option("--clean", help="Remove old exhibitions with the same name").flag("--keep", default = false)
    val exhibitionDescription by option("-d", "--description", help="Description of the exhibition").default("")
    val name by option("-n", "--name", help="The name of the exhibition. Shall be unique").default("default-name")
    val ignore by option("-i", "--ignore", help="Regex to ignore folders which are not part of the exhibition (other folders are treated as rooms)").default("__*")

    companion object{
        const val NORTH_WALL_NAME = "north"
        const val EAST_WALL_NAME = "east"
        const val SOUTH_WALL_NAME = "south"
        const val WEST_WALL_NAME = "west"
        const val ROOM_CONFIG_FILE = "room-config.json"
        const val WALL_CONFIG_FILE = "wall-config.json"
        const val PNG_EXTENSION = "png"
        const val JPG_EXTENSION = "jpg"
        const val JPEG_EXTENSION = "jpeg"
        const val JSON_EXTENSION = "json"

        val DEFAULT_ROOM_SIZE = Vector3f(10,5,10)
        val DEFAULT_ENTRY_POINT = Vector3f.ORIGIN
        val DEFAULT_ROOM_BORDER = .5f
        val DEFAULT_EXHIBITION_PADDING = 1f
        val DEFAULT_EXHIBIT_HEIGHT = 1.5f
    }

    override fun run() {
        val config = APIEndpoint.readConfig(this.config)
        val (reader, writer) = APIEndpoint.getDAOs(config.database)
        val exhibitionFolder = File(exhibitionPath)
        if(!exhibitionFolder.exists() and !exhibitionFolder.isDirectory){
            LOGGER.error("--path argument has to" + " point to an existing directory")
            exitProcess(-1)
       }
        if(reader.existsExhibition(name)){
            if(!clean){
                LOGGER.error("An exhibition with $name already exists. ")
                exitProcess(-2)
            }
            writer.deleteExhibition(name)
            LOGGER.info("Successfully removed previously existing exhibiiton '$name' ")
        }

        val exhibition = Exhibition(name= name, description = exhibitionDescription)
        LOGGER.info("Starting to import exhibition at $exhibitionFolder")
        exhibitionFolder.listFiles { file ->
            file.isDirectory and !file.nameWithoutExtension.matches(Regex.fromLiteral(ignore))
        }?.forEach { file ->

        }
    }

    private fun importRoom(root: Path, room:File, siblings:List<Room>){
        LOGGER.info("Importing room $room")
        val roomConfigFile = room.resolve(ROOM_CONFIG_FILE)
        var roomConfig = Room(room.name, "none", "none", DEFAULT_ROOM_SIZE, Vector3f.ORIGIN, DEFAULT_ENTRY_POINT)
        if(roomConfigFile.exists()){
            roomConfig = APIEndpoint.json.parse(Room.serializer(),roomConfigFile.readText())
        }
        
    }
}