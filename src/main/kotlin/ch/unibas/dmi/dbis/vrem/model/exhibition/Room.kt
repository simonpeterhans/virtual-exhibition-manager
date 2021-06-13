package ch.unibas.dmi.dbis.vrem.model.exhibition

import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import kotlinx.serialization.Serializable

/**
 * Object representation of exhibition rooms.
 *
 * @property text The title/description of the room.
 * @property floor The floor texture of the room.
 * @property ceiling The ceiling texture of the room.
 * @property position The position of the room.
 * @property size The size of the room.
 * @property entryPoint The spawning/entry point of the room.
 * @property ambient The ambient of the room.
 * @property exhibits A list of exhibits present in the room.
 * @property walls A list of walls present in the room.
 *                 Note that, usually, exhibits are part of walls and not of the exhibit list of a room object.
 * @constructor
 */
@Serializable
data class Room(
    val text: String,
    val floor: String = DEFAULT_FLOOR,
    val ceiling: String = DEFAULT_CEILING,
    var position: Vector3f = DEFAULT_POSITION,
    val size: Vector3f = DEFAULT_SIZE,
    val entryPoint: Vector3f = DEFAULT_ENTRYPOINT,
    val ambient: String? = null,
    val exhibits: MutableList<Exhibit> = mutableListOf(),
    val walls: MutableList<Wall> = mutableListOf()
) {

    companion object {
        val DEFAULT_SIZE = Vector3f(10, 5, 10)
        val DEFAULT_ENTRYPOINT = Vector3f.ORIGIN
        val DEFAULT_POSITION = Vector3f.ORIGIN
        const val DEFAULT_FLOOR = "NONE"
        const val DEFAULT_CEILING = "NONE"

        /**
         * Builds a room from a list of walls.
         *
         * @property text The title/description of the room.
         * @property floor The floor texture of the room.
         * @property ceiling The ceiling texture of the room.
         * @property position The position of the room.
         * @property size The size of the room.
         * @property entryPoint The spawning/entry point of the room.
         * @property ambient The ambient of the room.
         * @property walls A list of walls present in the room.
         * @return
         */
        fun build(
            text: String,
            floor: String,
            ceiling: String,
            size: Vector3f,
            position: Vector3f,
            entryPoint: Vector3f,
            ambient: String?,
            walls: List<Wall>
        ): Room {
            val room = Room(text, floor, ceiling, position, size, entryPoint, ambient)
            room.walls.addAll(walls)
            return room
        }
    }

    constructor(
        text: String,
        floor: String,
        ceiling: String,
        size: Vector3f,
        position: Vector3f,
        entrypoint: Vector3f
    ) : this(text, floor, ceiling, position, size, entrypoint, null)

    /**
     * Adds an exhibit to the room if it is not already in the room's list of exhibits.
     *
     * @param exhibit The exhibit to add.
     * @return True if the exhibit was successfully added, false otherwise (also in case of duplicates).
     */
    fun placeExhibit(exhibit: Exhibit): Boolean {
        if (exhibit.type != CulturalHeritageObject.Companion.CHOType.MODEL) {
            throw IllegalArgumentException("Only 3D objects can be placed in a room.")
        }
        return if (!exhibits.contains(exhibit)) {
            exhibits.add(exhibit)
        } else {
            false
        }
    }

    /**
     * Adds a wall with a given direction to the room.
     *
     * @param dir The direction of the wall.
     * @param w The wall to add.
     */
    private fun setWall(dir: Direction, w: Wall) {
        if (w.direction != dir) {
            throw IllegalArgumentException("Wall direction not matching. Expected $dir but got ${w.direction}")
        }
        walls.add(w)
    }

    /**
     * Obtains the wall object for a given direction for the room.
     *
     * @param dir The direction of the wall.
     * @return The wall facing the provided direction in the room.
     */
    private fun getWall(dir: Direction): Wall {
        try {
            return walls.first { w -> w.direction == dir }
        } catch (e: NoSuchElementException) {
            throw IllegalStateException("The room is corrupt. It does not have a wall with dir=$dir")
        }
    }

    fun getNorth(): Wall {
        return getWall(Direction.NORTH)
    }

    fun setNorth(wall: Wall) {
        setWall(Direction.NORTH, wall)
    }

    fun getEast(): Wall {
        return getWall(Direction.EAST)
    }

    fun setEast(wall: Wall) {
        setWall(Direction.EAST, wall)
    }

    fun getSouth(): Wall {
        return getWall(Direction.SOUTH)
    }

    fun setSouth(wall: Wall) {
        setWall(Direction.SOUTH, wall)
    }

    fun getWest(): Wall {
        return getWall(Direction.WEST)
    }

    fun setWest(wall: Wall) {
        setWall(Direction.WEST, wall)
    }

    override fun toString(): String {
        return "Room(text='$text', floor='$floor', ceiling='$ceiling', position=$position, size=$size, entrypoint=$entryPoint, ambient=$ambient, exhibits=$exhibits, walls=$walls)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        if (text != other.text) return false
        if (floor != other.floor) return false
        if (ceiling != other.ceiling) return false
        if (position != other.position) return false
        if (size != other.size) return false
        if (entryPoint != other.entryPoint) return false
        if (ambient != other.ambient) return false
        if (exhibits != other.exhibits) return false
        if (walls != other.walls) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + floor.hashCode()
        result = 31 * result + ceiling.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + entryPoint.hashCode()
        result = 31 * result + (ambient?.hashCode() ?: 0)
        result = 31 * result + exhibits.hashCode()
        result = 31 * result + walls.hashCode()
        return result
    }

}
