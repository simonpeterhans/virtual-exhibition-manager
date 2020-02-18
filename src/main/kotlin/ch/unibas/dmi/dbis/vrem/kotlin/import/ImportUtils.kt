package ch.unibas.dmi.dbis.vrem.kotlin.import

import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Wall
import ch.unibas.dmi.dbis.vrem.kotlin.model.math.Vector3f

/**
 * Collection of utilities for imports
 */
object ImportUtils {
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

    fun calculateRoomPosition(room: Room, siblings: List<Room>): Vector3f {
        return Vector3f(siblings.size, 0, 0)
    }

    fun calculateWallExhibitPosition(exhibit: Exhibit, siblings: List<Exhibit>, roomBorder: Float = .5f, exhbitPadding: Float = 1f, exhibitHeight: Float = 1.5f): Vector3f {
        if (siblings.isEmpty()) {
            return Vector3f(roomBorder + (exhibit.size.x / 2f), exhibitHeight)
        } else {
            val dist = siblings.map { it.size.x + exhbitPadding }.sum()
            return Vector3f(roomBorder + dist + (exhibit.size.x / 2f), exhibitHeight)
        }
    }

    fun copyDescription(src: Exhibit, dest: Exhibit, overwrite: Boolean = false) {
        if (src.description.isNotBlank()) {
            if (overwrite || dest.description.isBlank()) {
                dest.description = src.description
            }
        }
    }

    fun copyName(src: Exhibit, dest: Exhibit, overwrite: Boolean = false) {
        if (src.name.isNotBlank()) {
            if (overwrite || dest.name.isBlank()) {
                dest.name = src.name
            }
        }
    }

    fun findExhibitForPath(exhibition: Exhibition, path: String): Exhibit? {
        exhibition.rooms.forEach { room ->
            room.walls.flatMap { it.exhibits }.forEach {
                if (path == it.path) {
                    return it
                }
            }
        }
        return null
    }
}