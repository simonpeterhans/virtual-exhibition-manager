package ch.unibas.dmi.dbis.vrem.model.exhibition

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.*

@Serializable
data class Exhibition(
        @SerialName("_id") @Contextual
        val id: Id<Exhibition> = newId(),
        val name:String,
        val description:String = "",
        val rooms:MutableList<Room> = mutableListOf()
) {


    fun addRoom(room:Room): Boolean {
        return if(!rooms.contains(room)){
            rooms.add(room)
        }else{
            false
        }
    }


    @JsonIgnore
    fun getExhibits(): MutableList<Exhibit> {
        val exhibits = mutableListOf<Exhibit>()

        rooms.forEach { r ->
            exhibits.addAll(r.exhibits)
            exhibits.addAll(r.getNorth().exhibits)
            exhibits.addAll(r.getEast().exhibits)
            exhibits.addAll(r.getSouth().exhibits)
            exhibits.addAll(r.getWest().exhibits)
        }

        return Collections.unmodifiableList(exhibits)
    }

    @JsonIgnore
    fun getExhibits(type:CulturalHertiageObject.Companion.CHOType): List<Exhibit> {
        return getExhibits().filter { e -> e.type == type }
    }
}
