package ch.unibas.dmi.dbis.vrem.import

import ch.unibas.dmi.dbis.vrem.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.rest.APIEndpoint
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.float
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

/**
 * Importer for exhibition folders.
 * To be used directly as CLI command; the exhibition should include everything as in the
 * [sample exhibition](git@github.com:VIRTUE-DBIS/vre-mixnhack19.git).
 *
 * @constructor
 */
class ExhibitionFolderImporter : CliktCommand(name = "import-folder", help = "Imports a folder-based exhibition") {

    private val LOGGER = LogManager.getLogger(ExhibitionFolderImporter::class.java)

    val exhibitionPath by option("-p", "--path", help = "Path to the exhibition root folder").required()
    val config by option(
        "-c",
        "--config",
        help = "Relative of full path to the config file to be used"
    ).required()
    val clean by option("--clean", help = "Remove old exhibitions with the same name").flag(
        "--keep",
        default = false
    )
    val exhibitionDescription by option(
        "-d",
        "--description",
        help = "Description of the exhibition"
    ).default("")
    val name by option(
        "-n",
        "--name",
        help = "The name of the exhibition. Shall be unique"
    ).default("default-name")
    val ignore by option(
        "--ignore",
        help = "An ignore prefix to ignore folders and not treat them as a room folder"
    ).default("__")
    val defaultLongSide: Float by option(
        "--default-long-side",
        help = "The length of the long side of an image in meters"
    ).float().default(2f)

    lateinit var referenceExhibition: Exhibition
    lateinit var exhibition: Exhibition

    override fun run() {
        // Get config and a reader/writer for the database.
        val config = APIEndpoint.readConfig(this.config)
        val (reader, writer) = APIEndpoint.getDAOs(config.database)
        val exhibitionFolder = File(exhibitionPath)

        // Checks: Require exhibition folder to exist and no other exhibition with the same name to exist in the collection.
        if (!exhibitionFolder.exists() and !exhibitionFolder.isDirectory) {
            LOGGER.error("--path argument has to point to an existing directory")
            exitProcess(-1)
        }

        if (reader.existsExhibition(name)) {
            if (!clean) {
                LOGGER.error("An exhibition with $name already exists.")
                exitProcess(-2)
            }

            // If we made it here we can delete the exhibition from the collection.
            writer.deleteExhibition(name)
            LOGGER.info("Successfully removed previously existing exhibition '$name'.")
        }

        // Create new exhibition.
        this.exhibition = Exhibition(name = name, description = exhibitionDescription)
        val root = File(exhibitionPath)
        LOGGER.info("Starting import exhibition at $root.")

        // Try to add every folder as a new room to the previously created exhibition object.
        root.listFiles()?.filter { !it.nameWithoutExtension.startsWith(ignore) }
            ?.forEach { exhibition.addRoom(importRoom(root.parentFile, it, exhibition.rooms)) }

        // Save.
        writer.saveExhibition(exhibition)
        LOGGER.info("Finished import.")
    }

    /**
     * Tries to import a single exhibit.
     *
     * @param exhibitionRoot The root folder of the exhibition.
     * @param exhibitFile The configuration file of the exhibition.
     * @param siblings Sibling exhibits (in the same room).
     * @return The imported exhibit.
     */
    private fun importExhibit(exhibitionRoot: File, exhibitFile: File, siblings: List<Exhibit>): Exhibit {
        LOGGER.trace("Importing $exhibitFile.")
        val exhibit = readExhibitConfigOrCreateNew(exhibitionRoot, exhibitFile)
        val image = ImageIO.read(exhibitFile)
        val aspectRatio = image.height.toFloat() / image.width.toFloat()
        var width = defaultLongSide
        var height = defaultLongSide

        if (image.width > image.height) {
            height = (aspectRatio * (defaultLongSide * 100f)) / 100f // in cm for precision
        } else {
            width = ((defaultLongSide * 100f) / aspectRatio) / 100f // cm
        }

        if (exhibit.size.isNaN() or (exhibit.size == Vector3f.ORIGIN)) {
            exhibit.size = Vector3f(width, height)
        }

        if (exhibit.position.isNaN() or (exhibit.position == Vector3f.ORIGIN)) {
            exhibit.position = ImportUtils.calculateWallExhibitPosition(exhibit, siblings)
        }

        // TODO ???
        if (::referenceExhibition.isInitialized) {
            val ref = ImportUtils.findExhibitForPath(exhibition, exhibit.path)
            ref?.let {
                ImportUtils.copyName(it, exhibit)
                ImportUtils.copyDescription(it, exhibit)
            }
        }

        return exhibit
    }

    /**
     * Imports a room.
     *
     * @param root The root folder of the room.
     * @param roomFile The path of the room to read the config for.
     * @param siblings Sibling rooms, used to calculate a room's position.
     * @return The created room.
     */
    private fun importRoom(root: File, roomFile: File, siblings: List<Room>): Room {
        LOGGER.trace("Importing $roomFile.")
        val room = readRoomConfigOrCreateNew(roomFile)

        room.setNorth(importWall(Direction.NORTH, roomFile.resolve(ImportUtils.NORTH_WALL_NAME), root))
        room.setEast(importWall(Direction.EAST, roomFile.resolve(ImportUtils.EAST_WALL_NAME), root))
        room.setSouth(importWall(Direction.SOUTH, roomFile.resolve(ImportUtils.SOUTH_WALL_NAME), root))
        room.setWest(importWall(Direction.WEST, roomFile.resolve(ImportUtils.WEST_WALL_NAME), root))

        room.position = ImportUtils.calculateRoomPosition(room, siblings)
        return room
    }

    /**
     * Imports a wall and its exhibits.
     *
     * @param dir The direction the wall is facing when looking from the origin. // TODO Fixme.
     * @param wallFolder The path of the wall to read the config for.
     * @param root The root folder of the exhibition.
     * @return The wall (with the imported exhibits).
     */
    private fun importWall(dir: Direction, wallFolder: File, root: File): Wall {
        LOGGER.trace("Importing $wallFolder.")
        val wall = readWallConfigOrCreateNew(dir, wallFolder)
        wallFolder.listFiles()
            ?.filter { ImportUtils.IMAGE_FILE_EXTENSIONS.contains(it.extension) }
            ?.forEach { wall.placeExhibit(importExhibit(root, it, wall.exhibits)) }
        return wall
    }

    /**
     * Reads an exhibit configuration, creating a new one with default settings if the file was not found.
     *
     * @param exhibitionRoot The root folder of the exhibition (!).
     * @param exhibitFile The path of the exhibit to read the config for.
     * @return The exhibit as defined in the (potentially defaulted) configuration.
     */
    private fun readExhibitConfigOrCreateNew(exhibitionRoot: File, exhibitFile: File): Exhibit {
        val configFile =
            exhibitFile.parentFile.resolve("${exhibitFile.nameWithoutExtension}.${ImportUtils.JSON_EXTENSION}")
        LOGGER.trace("Looking for exhibit configuration at $configFile.")
        val path = exhibitFile.relativeTo(exhibitionRoot).toString().replace('\\', '/') // In case its Windows.
        return if (configFile.exists()) {
            val jsonString = configFile.readText()
            val exhibit = Json.decodeFromString(Exhibit.serializer(), jsonString)
            exhibit.path = path
            exhibit
        } else {
            Exhibit(exhibitFile.nameWithoutExtension, "", path, CulturalHertiageObject.Companion.CHOType.IMAGE)
        }
    }

    /**
     * Reads a wall configuration, creating a new one with default settings if the file was not found.
     *
     * @param dir The direction the wall is facing when looking from the origin. TODO Fixme.
     * @param wallFolder The path of the wall to read the config for.
     * @return The wall as defined in the (potentially defaulted) configuration.
     */
    private fun readWallConfigOrCreateNew(dir: Direction, wallFolder: File): Wall {
        val wallConfigFile = wallFolder.resolve(ImportUtils.WALL_CONFIG_FILE)
        LOGGER.trace("Looking for wall configuration at $wallConfigFile.")
        return if (wallConfigFile.exists()) {
            val jsonString = wallConfigFile.readText()
            val wall = Json.decodeFromString(Wall.serializer(), jsonString)
            wall.direction = dir
            wall
        } else {
            Wall(dir, "NONE")
        }
    }

    /**
     * Reads a room configuration, creating a new one with default settings if the file was not found.
     *
     * @param room The path of the room to read the config for.
     * @return The room as defined in the (potentially defaulted) configuration.
     */
    private fun readRoomConfigOrCreateNew(room: File): Room {
        val roomConfigFile = room.resolve(ImportUtils.ROOM_CONFIG_FILE)
        LOGGER.trace("Looking for room configuration at $room.")
        return if (roomConfigFile.exists()) {
            val jsonString = roomConfigFile.readText()
            Json.decodeFromString(Room.serializer(), jsonString)
        } else {
            Room(room.name, "NONE", "NONE", Vector3f.ORIGIN, Room.DEFAULT_SIZE, Room.DEFAULT_ENTRYPOINT)
        }
    }

}
