package ch.unibas.dmi.dbis.vrem.import

import ch.unibas.dmi.dbis.vrem.config.Config
import ch.unibas.dmi.dbis.vrem.database.dao.VREMDao
import ch.unibas.dmi.dbis.vrem.import.ImportUtils.calculateExhibitSize
import ch.unibas.dmi.dbis.vrem.import.ImportUtils.calculateWallExhibitPosition
import ch.unibas.dmi.dbis.vrem.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.float
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

/**
 * Importer for exhibition folders.
 * To be used directly as CLI command; the exhibition should include everything as in the
 * [sample exhibition](git@github.com:VIRTUE-DBIS/vre-mixnhack19.git).
 */
class ExhibitionFolderImporter : CliktCommand(name = "import-folder", help = "Imports a folder-based exhibition") {

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

    private lateinit var exhibition: Exhibition
    private lateinit var storageRoot: File
    private lateinit var importRoot: File

    companion object {
        private val json = Json {
            serializersModule = IdKotlinXSerializationModule
            encodeDefaults = true
        }
    }

    override fun run() {
        // Get config and a reader/writer for the database.
        val config = Config.readConfig(this.config)
        val (reader, writer) = VREMDao.getDAOs(config.database)
        val exhibitionFolder = File(exhibitionPath)

        // Checks: Require exhibition folder to exist and no other exhibition with the same name to exist.
        if (!exhibitionFolder.exists() and !exhibitionFolder.isDirectory) {
            logger.error { "--path argument has to point to an existing directory!" }
            exitProcess(-1)
        }

        if (reader.existsExhibition(name)) {
            if (!clean) {
                logger.error { "An exhibition with name $name already exists!" }
                exitProcess(-2)
            }

            // If we made it here we can delete the exhibition from the collection.
            writer.deleteExhibition(name)
            logger.info { "Successfully removed previously existing exhibition '$name'." }
        }

        // Create new exhibition.
        this.exhibition = Exhibition(name = name, description = exhibitionDescription)

        // Roots for import and storage.
        this.storageRoot = File(config.server.documentRoot)
        this.importRoot = File(exhibitionPath)

        // Determine path to copy media files to after importing.

        logger.info { "Starting import exhibition at ${this.importRoot}." }

        // Try to add every folder as a new room to the previously created exhibition object.
        this.importRoot.listFiles()?.filter { !it.nameWithoutExtension.startsWith(ignore) }
            ?.forEach { exhibition.addRoom(importRoom(it, exhibition.rooms)) }

        logger.info { "Writing exhibition entry to MongoDB..." }

        // Create MongoDB entry.
        writer.saveExhibition(exhibition)

        logger.info { "Copying media files..." }

        // Copy local files.
        exhibition.obtainExhibits().forEach {
            // Paths to copy to/from.
            val srcPath = importRoot.resolve(File(it.path))
            val targetPath = storageRoot.resolve(exhibition.id.toString()).resolve(it.path)

            // Create directories if they don't exist.
            Files.createDirectories(targetPath.parentFile.toPath())

            // Copy the files.
            Files.copy(srcPath.toPath(), targetPath.toPath())
        }

        // TODO Handle any errors upon MongoDB import or file copy.

        logger.info { "Finished import." }
    }

    /**
     * Tries to import a single exhibit.
     *
     * @param exhibitFile The exhibit file (e.g., an image).
     * @param siblings Sibling exhibits (in the same room).
     * @return The imported exhibit.
     */
    private fun importExhibit(exhibitFile: File, siblings: List<Exhibit>): Exhibit {
        logger.debug { "Importing exhibit $exhibitFile." }

        // Try to import exhibit by reading image and, if available, its config.
        val exhibit = readExhibitConfigOrCreateNew(exhibitFile)

        // No valid size given: Calculate exhibit size.
        if (exhibit.size.isNaN() or (exhibit.size == Vector3f.ORIGIN)) {
            calculateExhibitSize(exhibitFile.readBytes(), exhibit, defaultLongSide)
        }

        // Calculate exhibit position if not given.
        if (exhibit.position.isNaN() or (exhibit.position == Vector3f.ORIGIN)) {
            exhibit.position = calculateWallExhibitPosition(exhibit, siblings)
        }

        return exhibit
    }

    /**
     * Imports a room.
     *
     * @param roomFile The path of the room.
     * @param siblings Sibling rooms, used to calculate a room's position.
     * @return The created room.
     */
    private fun importRoom(roomFile: File, siblings: List<Room>): Room {
        logger.debug { "Importing room $roomFile." }

        val room = readRoomConfigOrCreateNew(roomFile)

        room.setNorth(importWall(Direction.NORTH, roomFile.resolve(ImportUtils.NORTH_WALL_NAME)))
        room.setEast(importWall(Direction.EAST, roomFile.resolve(ImportUtils.EAST_WALL_NAME)))
        room.setSouth(importWall(Direction.SOUTH, roomFile.resolve(ImportUtils.SOUTH_WALL_NAME)))
        room.setWest(importWall(Direction.WEST, roomFile.resolve(ImportUtils.WEST_WALL_NAME)))

        room.position = ImportUtils.calculateRoomPosition(room, siblings)

        return room
    }

    /**
     * Imports a wall and its exhibits.
     *
     * @param dir The direction the wall is facing when looking from the origin.
     * @param wallFolder The path of the wall to read the config for.
     * @return The wall (with the imported exhibits).
     */
    private fun importWall(dir: Direction, wallFolder: File): Wall {
        logger.debug { "Importing wall $wallFolder." }

        val wall = readWallConfigOrCreateNew(dir, wallFolder)

        wallFolder.listFiles()
            ?.filter { ImportUtils.IMAGE_FILE_EXTENSIONS.contains(it.extension.lowercase()) }
            ?.forEach { wall.placeExhibit(importExhibit(it, wall.exhibits)) }

        return wall
    }

    /**
     * Reads an exhibit configuration, creating a new one with default settings if the file was not found.
     *
     * @param exhibitFile The path of the exhibit to read the config for.
     * @return The exhibit as defined in the (potentially defaulted) configuration.
     */
    private fun readExhibitConfigOrCreateNew(exhibitFile: File): Exhibit {
        val configFile =
            exhibitFile.parentFile.resolve("${exhibitFile.nameWithoutExtension}.${ImportUtils.JSON_EXTENSION}")

        logger.debug { "Looking for exhibit configuration at $configFile." }

        val exhibitPath = exhibitFile.relativeTo(importRoot).toString().replace('\\', '/') // In case its Windows.

        return if (configFile.exists()) {
            val exhibit = json.decodeFromString(Exhibit.serializer(), configFile.readText())

            exhibit.path = exhibitPath
            exhibit
        } else {
            Exhibit(exhibitFile.nameWithoutExtension, exhibitPath, CulturalHeritageObject.Companion.CHOType.IMAGE)
        }
    }

    /**
     * Reads a wall configuration, creating a new one with default settings if the file was not found.
     *
     * @param dir The direction the wall is facing when looking from the origin.
     * @param wallFolder The path of the wall to read the config for.
     * @return The wall as defined in the (potentially defaulted) configuration.
     */
    private fun readWallConfigOrCreateNew(dir: Direction, wallFolder: File): Wall {
        val wallConfigFile = wallFolder.resolve(ImportUtils.WALL_CONFIG_FILE)

        logger.debug { "Looking for wall configuration at $wallConfigFile." }

        return if (wallConfigFile.exists()) {
            val wall = json.decodeFromString(Wall.serializer(), wallConfigFile.readText())

            wall.direction = dir
            wall
        } else {
            Wall(dir)
        }
    }

    /**
     * Reads a room configuration, creating a new one with default settings if the file was not found.
     *
     * @param room The path of the room to read the config for.
     * @return The room as defined in the (potentially defaulted) configuration.
     */
    private fun readRoomConfigOrCreateNew(room: File): Room {
        logger.debug { "Looking for room configuration at $room." }

        val roomConfigFile = room.resolve(ImportUtils.ROOM_CONFIG_FILE)

        return if (roomConfigFile.exists()) {
            json.decodeFromString(Room.serializer(), roomConfigFile.readText())
        } else {
            Room(room.name)
        }
    }

}
