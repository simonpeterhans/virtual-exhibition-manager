package ch.unibas.dmi.dbis.vrem.gen

import ch.unibas.dmi.dbis.vrem.config.Config
import ch.unibas.dmi.dbis.vrem.config.DatabaseConfig
import ch.unibas.dmi.dbis.vrem.config.WebServerConfig
import ch.unibas.dmi.dbis.vrem.import.ExhibitionFolderImporter
import ch.unibas.dmi.dbis.vrem.import.ImportUtils
import ch.unibas.dmi.dbis.vrem.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.rest.APIEndpoint
import io.javalin.http.Context
import org.apache.logging.log4j.LogManager
import java.io.File
import javax.imageio.ImageIO

/**
 * Primitive collection generator.
 * Does currently not store or read any configuration files;
 * if this ever becomes desirable, consider using methods from ExhibitionFolderImporter.
 *
 * @constructor Create empty Collection generator
 */
class CollectionGenerator {

    // Static stuff.
    companion object {
        private val LOGGER = LogManager.getLogger(ExhibitionFolderImporter::class.java)

        // TODO Make those adjustable parameters for generateRandomExhibition().
        private const val EXHIBITION_PATH =
            "../vre-random/images" // Path to the folder of the images (= parent of the image folder).
        private const val NUM_IMAGES_PER_WALL = 4
        private const val NUM_WALLS = 4 // This is currently rather redundant.
        private val DEFAULT_TYPE = CulturalHeritageObject.Companion.CHOType.IMAGE

        private const val DEFAULT_LONG_SIDE_METERS = 2.0

        private const val JPG_EXTENSION = "jpg"
        private const val JPEG_EXTENSION = "jpeg"
        private const val PNG_EXTENSION = "png"
        private const val BMP_EXTENSION = "bmp"
        private val IMAGE_FILE_EXTENSIONS = listOf(JPEG_EXTENSION, JPG_EXTENSION, PNG_EXTENSION, BMP_EXTENSION)
    }

    private fun loadImageToExhibit(root: File, exhibitFile: File): Exhibit {
        val relativePath = exhibitFile.relativeTo(root).toString().replace('\\', '/') // In case its Windows.
        val exhibit = Exhibit(exhibitFile.nameWithoutExtension, "", relativePath, DEFAULT_TYPE)

        // Load image as stream so we don't have to load the entire thing.
        val imageStream = ImageIO.createImageInputStream(exhibitFile)
        val reader = ImageIO.getImageReaders(imageStream).next()
        reader.input = imageStream

        // Get width and height.
        val imageHeight = reader.getWidth(0)
        val imageWidth = reader.getHeight(0)

        val aspectRatio = imageHeight.toFloat() / imageWidth.toFloat()
        var width = DEFAULT_LONG_SIDE_METERS
        var height = DEFAULT_LONG_SIDE_METERS

        if (imageWidth > imageHeight) {
            height = (aspectRatio * (DEFAULT_LONG_SIDE_METERS * 100.0)) / 100.0
        } else {
            width = ((DEFAULT_LONG_SIDE_METERS * 100.0) / aspectRatio) / 100.0
        }

        if (exhibit.size.isNaN() or (exhibit.size == Vector3f.ORIGIN)) {
            exhibit.size = Vector3f(width, height)
        }

        return exhibit
    }

    private fun selectRandomExhibits(num_walls: Int = NUM_WALLS): MutableList<Exhibit> {
        val exhibits = mutableListOf<Exhibit>()

        val root = File(EXHIBITION_PATH)
        LOGGER.info("Scanning for images at $root.")

        val files = root.listFiles()?.filter { IMAGE_FILE_EXTENSIONS.contains(it.extension) } ?: return exhibits

        // List files and shuffle.
        var fileList = files.toMutableList()
        fileList.shuffle()

        if (files.size > num_walls * NUM_IMAGES_PER_WALL) {
            fileList = fileList.subList(0, 4 * NUM_IMAGES_PER_WALL)
        }

        // Only process filtered list.
        fileList.forEach { exhibits.add(loadImageToExhibit(root.parentFile, it)) }

        return exhibits
    }

    /*private fun preprocessExhibit(exhibit: Exhibit, siblings: List<Exhibit>) {
        // This is currently an 1:1 implementation of calculateWallExhibitPosition(), consider replacing if using this.
        if (siblings.isEmpty()) { // First image, no distance.
            exhibit.position = Vector3f(0.5 + (exhibit.size.x / 2.0), 1.5)
        } else {
            val dist = siblings.sumOf { it.size.x + 1.0 }
            exhibit.position = Vector3f(0.5 + dist + (exhibit.size.x / 2.0), 1.5)
        }
    }*/

    private fun generateWalls(): MutableList<Wall> {
        // Create walls.
        val walls = mutableListOf<Wall>()

        walls.add(Wall(Direction.NORTH, "CONCRETE"))
        walls.add(Wall(Direction.SOUTH, "CONCRETE"))
        walls.add(Wall(Direction.EAST, "CONCRETE"))
        walls.add(Wall(Direction.WEST, "CONCRETE"))

        // Get random images.
        val exhibits = selectRandomExhibits(NUM_WALLS)

        // Place them on every wall.
        for (w in walls) {
            for (i in 1..NUM_IMAGES_PER_WALL) {
                val exhibit = exhibits.removeFirst()

                exhibit.position = ImportUtils.calculateWallExhibitPosition(exhibit, w.exhibits)

                w.exhibits.add(exhibit)
            }
        }

        return walls
    }

    private fun generateRooms(): MutableList<Room> {
        val rooms = mutableListOf<Room>()

        val room = Room(
            "Randomized Room",
            "WoodenDarkHParquetFloor",
            "WhiteStucco",
            Room.DEFAULT_SIZE,
            Vector3f.ORIGIN,
            Room.DEFAULT_ENTRYPOINT
        )

        room.walls.addAll(generateWalls())
        rooms.add(room)

        return rooms
    }

    fun generateRandomExhibition(save: Boolean = false): Exhibition {
        val exhibition = Exhibition(name = "Randomized Collection")

        // Generate rooms.
        val rooms = generateRooms()

        //  Add rooms.
        for (r in rooms) {
            exhibition.addRoom(r)
        }

        if (save) {
            val config = Config(
                DatabaseConfig("127.0.0.1", 27017, "vrem"),
                WebServerConfig("../vre-random", 4567)
            )
            val (_, writer) = APIEndpoint.getDAOs(config.database)

            writer.saveExhibition(exhibition)
        }

        return exhibition
    }

    fun generateRandomExhibitionForContext(ctx: Context) {
        LOGGER.info("Generating random collection...")

        ctx.json(generateRandomExhibition())
    }

}
