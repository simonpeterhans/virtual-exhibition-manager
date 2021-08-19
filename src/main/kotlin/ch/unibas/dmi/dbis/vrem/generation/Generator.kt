package ch.unibas.dmi.dbis.vrem.generation

import ch.unibas.dmi.dbis.vrem.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.rest.handlers.RequestContentHandler
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.*

abstract class Generator(val cineastHttp: CineastHttp) {

    abstract fun genRoom(): Room

    abstract fun genExhibition(): Exhibition

    fun roomSizeFromWalls(
        walls: List<Wall>,
        padding: Double = 1.5,
        baseHeight: Double = 2.0
    ): Vector3f {
        var xDimLen = 0.0
        var yDimLen = 6.0 // Minimum height.
        var zDimLen = 0.0

        for (w in walls) {
            val lastPos = w.exhibits.last().position

            // Use exhibit x coordinate for both dimensions since it's relative to the wall, and not the world.
            if (w.direction.axis == Direction.Coordinate.X) {
                xDimLen = max(xDimLen, lastPos.x + padding)
                yDimLen = max(yDimLen, lastPos.y.toDouble() + baseHeight)
            } else if (w.direction.axis == Direction.Coordinate.Z) {
                zDimLen = max(zDimLen, lastPos.x + padding)
                yDimLen = max(yDimLen, lastPos.y.toDouble() + baseHeight)
            }
        }

        return Vector3f(xDimLen, yDimLen, zDimLen)
    }

    fun idListToExhibits(ids: List<String?>): MutableList<Exhibit> {
        val exhibits = mutableListOf<Exhibit>()

        for (id in ids) {
            var e: Exhibit

            if (id == null) {
                e = Exhibit(name = "Empty Exhibit", path = "")
                e.size = Vector3f(1.0, 1.0)
            } else {
                e = Exhibit(name = id, path = id + RequestContentHandler.URL_ID_SUFFIX)

                val imageBytes = cineastHttp.objectRequest(id)

                calculateExhibitSize(imageBytes, e, 2.0)
            }

            exhibits.add(e)
        }

        return exhibits
    }

    fun exhibitListToWalls(dims: IntArray, exhibitList: MutableList<Exhibit>): MutableList<Wall> {
        val walls = mutableListOf<Wall>()

        // Add a wall for every direction.
        enumValues<Direction>().forEach { e ->
            walls.add(Wall(e, "CONCRETE"))
        }

        // Place exhibits on walls (this will only work for 2D setups with 4 walls).
        for (i in 0 until dims[0]) {
            for (w in 0 until walls.size) {
                for (j in 0 until dims[1] / walls.size) {
                    val exhibit = exhibitList.removeFirst()

                    exhibit.position = getWallPositionByCoords(i, j)

                    walls[w].exhibits.add(exhibit)
                }
            }
        }

        return walls
    }

    fun printAnglesForEx(ex: Exhibition) {
        // TODO If we're in the original (center) room, use the angle of the exhibit, otherwise, use the angle of the center of the room.
        for (r in ex.rooms) {
            for (w in r.walls) {
                for (e in w.exhibits) {
                    val offsetAngle = 0.25 * Math.PI
                    val newX = cos(offsetAngle) * 0.5 * r.size.x - sin(offsetAngle) * (e.position.x - 0.5 * r.size.x)
                    val newZ = cos(offsetAngle) * (e.position.x - 0.5 * r.size.x) + sin(offsetAngle) * 0.5 * r.size.x
                    println(Math.toDegrees(0.5 * Math.PI * w.direction.ordinal + (0.5 * PI - atan(newZ / newX))))
                }
            }
        }
    }

    fun calculateExhibitSize(exhibitFile: ByteArray, exhibit: Exhibit, defaultLongSide: Double) {
        // Load image as stream so that we don't have to load the entire thing.
        val imageStream = ImageIO.createImageInputStream(ByteArrayInputStream(exhibitFile))

        val reader = ImageIO.getImageReaders(imageStream).next()
        reader.input = imageStream

        // Get width and height.
        val imageWidth = reader.getWidth(0)
        val imageHeight = reader.getHeight(0)

        // Close stream.
        imageStream.close()

        // Calculate aspect ratio and adjust width/height accordingly.
        val aspectRatio = imageHeight.toFloat() / imageWidth

        var width = defaultLongSide
        var height = defaultLongSide

        if (imageWidth > imageHeight) {
            height = (aspectRatio * (defaultLongSide * 100.0)) / 100.0 // Convert to cm for precision.
        } else {
            width = ((defaultLongSide * 100.0) / aspectRatio) / 100.0 // Convert to cm for precision.
        }

        // Update width/height.
        exhibit.size = Vector3f(width, height)
    }

    fun getWallPositionByCoords(
        row: Int,
        column: Int,
        padding: Double = 1.5, // Towards other exhibits and walls.
        baseHeight: Double = 2.0, // Offset towards the ground.
        longerSideLength: Double = 2.0 // Maximum length/height of an exhibit.
    ): Vector3f {
        return Vector3f(
            (longerSideLength + padding) * column + padding,
            (longerSideLength + padding) * row + baseHeight,
            0.0
        )
    }

}
