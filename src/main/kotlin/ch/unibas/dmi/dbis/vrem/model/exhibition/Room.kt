package ch.unibas.dmi.dbis.vrem.model.exhibition

import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Serializable
import java.util.*

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
@Serializable
data class Room(
        val text:String,
        val floor:String = DEFAULT_FLOOR,
        val ceiling:String = DEFAULT_CEILING,
        var position:Vector3f = DEFAULT_POSITION,
        val size:Vector3f = DEFAULT_SIZE,
        val entrypoint:Vector3f = DEFAULT_ENTRYPOINT,
        val ambient:String? = null,
        val exhibits:MutableList<Exhibit> = mutableListOf(),
        val walls:MutableList<Wall> = mutableListOf()
) {
    companion object{
        fun build(text: String,floor: String,ceiling: String,size: Vector3f,position: Vector3f,entrypoint: Vector3f,ambient: String?,walls:List<Wall>): Room {
            val room = Room(text,floor,ceiling,position,size,entrypoint,ambient)
            room.walls.addAll(walls)
            return room
        }

        val DEFAULT_SIZE = Vector3f(10,5,10)
        val DEFAULT_ENTRYPOINT = Vector3f.ORIGIN
        val DEFAULT_POSITION = Vector3f.ORIGIN
        const val DEFAULT_FLOOR = "NONE"
        const val DEFAULT_CEILING = "NONE"
    }


    constructor(text: String,floor: String,ceiling: String,size: Vector3f,position: Vector3f,entrypoint: Vector3f):this(text, floor, ceiling, position, size, entrypoint, null)

    fun placeExhibit(exhibit: Exhibit):Boolean{
        if(exhibit.type != CulturalHertiageObject.Companion.CHOType.MODEL){
            throw IllegalArgumentException("Only 3D objects can be placed in a room.")
        }
        return if(!exhibits.contains(exhibit)){
            exhibits.add(exhibit)
        }else{
            false
        }
    }

    @JsonIgnore
    private fun setWall(dir:Direction, w:Wall){
        if(w.direction != dir){
            throw IllegalArgumentException("Wall direction not matching. Expected $dir but got ${w.direction}")
        }
        walls.add(w)
    }

    @JsonIgnore
    private fun getWall(dir:Direction):Wall{
        try {
            return walls.first {w -> w.direction == dir}
        }catch (e: NoSuchElementException){
            throw IllegalStateException("The room is corrupt. It does not have a wall with dir=$dir")
        }
    }

    @JsonIgnore
    fun getNorth():Wall{
        return getWall(Direction.NORTH)
    }

    @JsonIgnore
    fun setNorth(wall:Wall){
        setWall(Direction.NORTH, wall)
    }

    @JsonIgnore
    fun getEast():Wall{
        return getWall(Direction.EAST)
    }

    @JsonIgnore
    fun setEast(wall:Wall){
        setWall(Direction.EAST, wall)
    }

    @JsonIgnore
    fun getSouth():Wall{
        return getWall(Direction.SOUTH)
    }

    @JsonIgnore
    fun setSouth(wall:Wall){
        setWall(Direction.SOUTH,wall)
    }

    @JsonIgnore
    fun getWest(): Wall {
        return getWall(Direction.WEST)
    }

    @JsonIgnore
    fun setWest(wall:Wall){
        setWall(Direction.WEST,wall)
    }

    override fun toString(): String {
        return "Room(text='$text', floor='$floor', ceiling='$ceiling', position=$position, size=$size, entrypoint=$entrypoint, ambient=$ambient, exhibits=$exhibits, walls=$walls)"
    }

    override fun equals(other: Any?): Boolean {
        // IntelliJ IDEA generated
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        if (text != other.text) return false
        if (floor != other.floor) return false
        if (ceiling != other.ceiling) return false
        if (position != other.position) return false
        if (size != other.size) return false
        if (entrypoint != other.entrypoint) return false
        if (ambient != other.ambient) return false
        if (exhibits != other.exhibits) return false
        if (walls != other.walls) return false

        return true
    }

    override fun hashCode(): Int {
        // IntelliJ IDEA generated
        var result = text.hashCode()
        result = 31 * result + floor.hashCode()
        result = 31 * result + ceiling.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + entrypoint.hashCode()
        result = 31 * result + (ambient?.hashCode() ?: 0)
        result = 31 * result + exhibits.hashCode()
        result = 31 * result + walls.hashCode()
        return result
    }


}