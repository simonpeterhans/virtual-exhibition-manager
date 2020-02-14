package ch.unibas.dmi.dbis.vrem.kotlin.import

import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.kotlin.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.kotlin.rest.APIEndpoint
import ch.unibas.dmi.dbis.vrem.kotlin.rest.APIEndpoint.Companion.json
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.float
import org.apache.logging.log4j.LogManager
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

class ExhibitionFolderImporter : CliktCommand(name="import-folder", help="Imports a folder-based exhibition"){

    private val LOGGER = LogManager.getLogger(ExhibitionFolderImporter::class.java)

    val exhibitionPath by option("-p", "--path", help = "Path to the exhibition root folder").required()
    val config by option("-c", "--config", help="Relative of full path to the config file to be used").required()
    val clean by option("--clean", help="Remove old exhibitions with the same name").flag("--keep", default = false)
    val exhibitionDescription by option("-d", "--description", help="Description of the exhibition").default("")
    val name by option("-n", "--name", help="The name of the exhibition. Shall be unique").default("default-name")
    val ignore by option("--ignore", help="An ignore prefix to ignore folders and not treat them as a room folder").default("__")
    val defaultLongSide : Float by option("--default-long-side", help="The length of the long side of an image in meters").float().default(2f)

    lateinit var referenceExhibition:Exhibition
    lateinit var exhibition:Exhibition

    companion object {
        const val WALL_CONFIG_FILE = "wall-config.json"
        const val ROOM_CONFIG_FILE = "room-config.json"

        val DEFAULT_ROOM_SIZE = Vector3f(10,5,10)
        val DEFAULT_ROOM_ENTRYPOINT = Vector3f.ORIGIN

        const val NORTH_WALL_NAME = "north"
        const val EAST_WALL_NAME = "east"
        const val SOUTH_WALL_NAME = "south"
        const val WEST_WALL_NAME = "west"

        const val JSON_EXTENSION = "json"
        const val JPG_EXTENSION = "jpg"
        const val JPEG_EXTENSION = "jpeg"
        const val PNG_EXTENSION = "png"
        const val BMP_EXTENSION = "bmp"
        val IMAGE_FILE_EXTENSIONS = listOf(JPEG_EXTENSION, JPG_EXTENSION, PNG_EXTENSION, BMP_EXTENSION)
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

        this.exhibition = Exhibition(name= name, description = exhibitionDescription)
        val root = File(exhibitionPath)
        LOGGER.info("Startin import exhibition at $root")
        root.listFiles()?.filter { !it.nameWithoutExtension.startsWith(ignore) }
                ?.forEach { exhibition.addRoom(importRoom(root.parentFile, it, exhibition.rooms)) }
        writer.saveExhibition(exhibition)
        LOGGER.info("Finished import")
    }

    private fun importExhibit(exhibitionRoot:File, exhibitFile: File, siblings: List<Exhibit>): Exhibit {
        LOGGER.trace("Importing $exhibitFile")
        val exhibit = readExhibitConfigOrCreateNew(exhibitionRoot, exhibitFile)
        val image = ImageIO.read(exhibitFile)
        val aspectRatio = image.height.toFloat() / image.width.toFloat()
        var width = defaultLongSide
        var height = defaultLongSide
        if(image.width > image.height){
            height = (aspectRatio * (defaultLongSide*100f)) / 100f // in cm for precision
        }else{
            width = ((defaultLongSide*100f)/aspectRatio)/100f // cm
        }
        if(exhibit.size.isNaN() or exhibit.size.equals(Vector3f.ORIGIN)){
            exhibit.size = Vector3f(width,height)
        }
        if(exhibit.position.isNaN() or exhibit.position.equals(Vector3f.ORIGIN)){
            exhibit.position = ImportUtils.calculateWallExhibitPosition(exhibit, siblings)
        }
        if(::referenceExhibition.isInitialized){
            val ref = ImportUtils.findExhibitForPath(exhibition, exhibit.path)
            ref?.let {
                ImportUtils.copyName(it, exhibit)
                ImportUtils.copyDescription(it, exhibit)
            }
        }
        return exhibit
    }

    private fun importRoom(root:File, roomFile:File, siblings: List<Room>): Room {
        LOGGER.trace("Importing $roomFile")
        val room = readRoomConfigOrCreateNew(roomFile)

        room.setNorth(importWall(Direction.NORTH, roomFile.resolve(NORTH_WALL_NAME), root))
        room.setEast(importWall(Direction.EAST, roomFile.resolve(EAST_WALL_NAME), root))
        room.setSouth(importWall(Direction.SOUTH, roomFile.resolve(SOUTH_WALL_NAME), root))
        room.setWest(importWall(Direction.WEST, roomFile.resolve(WEST_WALL_NAME), root))

        room.position = ImportUtils.calculateRoomPosition(room, siblings)
        return room
    }

    private fun importWall(dir: Direction, wallFolder:File, root:File): Wall {
        LOGGER.trace("Importing $wallFolder")
        val wall = readWallConfigOrCreateNew(dir, wallFolder)
        wallFolder.listFiles()
                ?.filter { IMAGE_FILE_EXTENSIONS.contains(it.extension) }
                ?.forEach { wall.placeExhibit(importExhibit(root, it, wall.exhibits)) }
        return wall
    }

    private fun readExhibitConfigOrCreateNew(exhibitionRoot: File, exhibitFile:File): Exhibit {
        val configFile = exhibitFile.parentFile.resolve("${exhibitFile.nameWithoutExtension}.$JSON_EXTENSION")
        LOGGER.trace("Looking for exhibit configuration at $configFile")
        val path = exhibitFile.relativeTo(exhibitionRoot).toString().replace('\\', '/') // In case its windows
        return if(configFile.exists()){
            val jsonString = configFile.readText()
            val exhibit = json.parse(Exhibit.serializer(), jsonString)
            exhibit.path = path
            exhibit
        }else{
             Exhibit(exhibitFile.nameWithoutExtension, "", path, CulturalHertiageObject.Companion.CHOType.IMAGE)
        }
    }

    private fun readWallConfigOrCreateNew(dir:Direction, wallFolder:File): Wall {
        val wallConfigFile = wallFolder.resolve(WALL_CONFIG_FILE)
        LOGGER.trace("Looking for wall configuration at $wallConfigFile")
        return if(wallConfigFile.exists()){
            val jsonString = wallConfigFile.readText()
            val wall = json.parse(Wall.serializer(), jsonString)
            wall.direction = dir
            wall
        }else{
            Wall(dir, "NONE")
        }
    }

    private fun readRoomConfigOrCreateNew(room:File): Room {
        val roomConfigFile = room.resolve(ROOM_CONFIG_FILE)
        LOGGER.trace("Looking for room configuration at $room")
        return if(roomConfigFile.exists()){
            val josnString = roomConfigFile.readText()
            return json.parse(Room.serializer(), josnString)
        }else{
            return Room(room.name, "NONE", "NONE", Vector3f.ORIGIN, DEFAULT_ROOM_SIZE, DEFAULT_ROOM_ENTRYPOINT)
        }
    }
}