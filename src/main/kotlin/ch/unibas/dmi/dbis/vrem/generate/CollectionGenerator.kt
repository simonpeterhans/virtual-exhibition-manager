package ch.unibas.dmi.dbis.vrem.generate

import ch.unibas.dmi.dbis.vrem.config.Config
import ch.unibas.dmi.dbis.vrem.config.DatabaseConfig
import ch.unibas.dmi.dbis.vrem.config.WebServerConfig
import ch.unibas.dmi.dbis.vrem.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.rest.APIEndpoint
import io.javalin.http.Context
import org.apache.logging.log4j.LogManager
import java.io.File
import javax.imageio.ImageIO

/**
 * Primitive collection generator.
 * Does currently not store or read any configuration files and is relatively independent of the import component.
 *
 * TODO Fix this after refactoring collection storage handling.
 *
 * @constructor
 */
class CollectionGenerator {

    // Static stuff.
    companion object {
        private val LOGGER = LogManager.getLogger(CollectionGenerator::class.java)

        /*
         * TODO Avoid redefining many of the parameters below (duplicates of the import package)
         *  when refactoring this once we have a clearer structure of the overall role of the generator.
         */
        // Path to the folder of the images (= parent of the image folder).
        private const val EXHIBITION_PATH = "../vre-random/images"

        // Room properties.
        private const val NUM_IMAGES_PER_WALL = 5
        private const val NUM_WALLS = 4 // This is currently rather redundant.

        private val DEFAULT_TYPE = CulturalHeritageObject.Companion.CHOType.IMAGE
        private const val DEFAULT_LONG_SIDE_METERS = 2.0
        private const val RANDOMIZED_ROOM_TEXT = "Randomized Room"
        private const val DEFAULT_FLOOR = "WoodenDarkHParquetFloor"
        private const val DEFAULT_CEIL = "WhiteStucco"

        private const val JPG_EXTENSION = "jpg"
        private const val JPEG_EXTENSION = "jpeg"
        private const val PNG_EXTENSION = "png"
        private const val BMP_EXTENSION = "bmp"
        private val IMAGE_FILE_EXTENSIONS = listOf(JPEG_EXTENSION, JPG_EXTENSION, PNG_EXTENSION, BMP_EXTENSION)
    }

    /**
     * Loads an image file into an exhibit object.
     * Uses the same adjustment for overshooting width/height like ExhibitionFolderImporter.
     *
     * @param root The root path of the exhibition.
     * @param exhibitFile The image file to create the exhibit object for.
     * @return The created exhibit object with width/height set.
     */
    private fun loadImageToExhibit(root: File, exhibitFile: File): Exhibit {
        val relativePath = exhibitFile.relativeTo(root).toString().replace('\\', '/') // In case its Windows.
        val exhibit = Exhibit(exhibitFile.nameWithoutExtension, relativePath, DEFAULT_TYPE)

        // Load image as stream so we don't have to load the entire thing.
        val imageStream = ImageIO.createImageInputStream(exhibitFile)

        val reader = ImageIO.getImageReaders(imageStream).next()
        reader.input = imageStream

        // Get width and height.
        val imageHeight = reader.getWidth(0)
        val imageWidth = reader.getHeight(0)

        // Close stream.
        imageStream.close()

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

    /**
     * Randomly selects a number of exhibits for a given number of walls and exhibits per wall from a folder of images.
     *
     * @param num_walls The number of walls to consider (can be used to override NUM_WALLS)
     * @return A list of the selected and loaded exhibit objects.
     */
    private fun selectRandomExhibits(num_walls: Int = NUM_WALLS): MutableList<Exhibit> {
        val exhibits = mutableListOf<Exhibit>()

        val root = File(EXHIBITION_PATH)

        LOGGER.info("Generating random collection for images at $root.")

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

    /**
     * Calculates the position of an exhibit on the wall depending on the number of siblings.
     * This is currently a simplified version of calculateWallExhibitPosition().
     *
     * @param exhibit The exhibit to calculate and set the position for.
     * @param siblings A list of siblings of the exhibit (exhibits on the same wall).
     */
    private fun calcExhibitWallPosition(exhibit: Exhibit, siblings: List<Exhibit>) {
        // This is currently a clone of calculateWallExhibitPosition(), consider replacing if using this.
        if (siblings.isEmpty()) { // First image, no distance.
            exhibit.position = Vector3f(0.5 + (exhibit.size.x / 2.0), 1.5)
        } else {
            val dist = siblings.sumOf { it.size.x + 1.0 }
            exhibit.position = Vector3f(0.5 + dist + (exhibit.size.x / 2.0), 1.5)
        }
    }

    /**
     * Generates the walls for a collection, along with the images hanging on them.
     *
     * @return The list of the created wall objects.
     */
    private fun generateWalls(): MutableList<Wall> {
        // Create walls.
        val walls = mutableListOf<Wall>()

        // Add 1 wall for every direction.
        enumValues<Direction>().forEach { e -> walls.add(Wall(e, "CONCRETE")) }

        // Get random images.
        val exhibits = selectRandomExhibits(NUM_WALLS)

        // Place them on every wall.
        for (w in walls) {
            for (i in 1..NUM_IMAGES_PER_WALL) {
                val exhibit = exhibits.removeFirst()

                //exhibit.position = ImportUtils.calculateWallExhibitPosition(exhibit, w.exhibits)
                calcExhibitWallPosition(exhibit, w.exhibits)

                w.exhibits.add(exhibit)
            }
        }

        return walls
    }

    /**
     * Generates a list of rooms with 4 walls and randomized exhibits.
     * Since we currently only use single room setups, this will always be a list containing a single object for now.
     *
     * @return The list of generated rooms.
     */
    private fun generateRooms(): MutableList<Room> {
        val rooms = mutableListOf<Room>()

        val room = Room(RANDOMIZED_ROOM_TEXT)

        room.walls.addAll(generateWalls())
        rooms.add(room)

        return rooms
    }

    /**
     * Generates a random exhibition.
     *
     * @param save Whether to store the randomly generated exhibition or not.
     * @return The newly created exhibition object.
     */
    fun generateRandomExhibition(save: Boolean = true): Exhibition {
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

    /**
     * Wrapper for generateRandomExhibition() in a request context.
     *
     * @param ctx The request context object.
     */
    fun generateRandomExhibitionForContext(ctx: Context) {
        LOGGER.info("Generating random collection...")

        ctx.json(generateRandomExhibition())
    }

}
