package ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition

import org.bson.types.ObjectId
import java.util.*

class Exhibition(
        val id:String,
        val name:String,
        val description:String
) {
    private val rooms = mutableListOf<Room>()

    constructor(name: String,description: String):this(ObjectId().toHexString(),name,description)

    fun addRoom(room:Room): Boolean {
        return if(!rooms.contains(room)){
            rooms.add(room)
        }else{
            false
        }
    }

    fun getRooms(): MutableList<Room> {
        return Collections.unmodifiableList(rooms)
    }

    fun getExhibits(): MutableList<Exhibit> {
        val exhibits = mutableListOf<Exhibit>()

        rooms.forEach { r ->
            exhibits.addAll(r.getExhibits())
            exhibits.addAll(r.getNorth().getExhibits())
            exhibits.addAll(r.getEast().getExhibits())
            exhibits.addAll(r.getSouth().getExhibits())
            exhibits.addAll(r.getWest().getExhibits())
        }

        return Collections.unmodifiableList(exhibits)
    }

    fun getExhibits(type:CulturalHertiageObject.Companion.CHOType): List<Exhibit> {
        return getExhibits().filter { e -> e.type == type }
    }
}
