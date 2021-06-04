package ch.unibas.dmi.dbis.vrem.model.exhibition

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.*

/**
 * Exhibition object.
 *
 * TODO Fix IDs.
 *
 * @property id The ID of the exhibition (using kmongo to create the ID).
 * @property name The name of the exhibition.
 * @property description The description of the exhibition.
 * @property rooms A list of rooms that the exhibition consists of.
 * @constructor
 */
@Serializable
data class Exhibition(
    @SerialName("_id") @Contextual
    val id: Id<Exhibition> = newId(),
    val name: String,
    val description: String = "",
    val rooms: MutableList<Room> = mutableListOf()
) {
    /**
     * Adds a room to the exhibition if it is not already present.
     *
     * @param room The room to add.
     * @return True if the room was successfully added, false otherwise.
     */
    fun addRoom(room: Room): Boolean {
        return if (!rooms.contains(room)) {
            rooms.add(room)
        } else {
            false
        }
    }

    /**
     * Obtains all exhibits from all rooms of the exhibition.
     *
     * @return A list of all exhibits.
     */
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

    /**
     * Obtains all exhibits of a certain type from the exhibition.
     *
     * @param type The CHO type to obtain.
     * @return A list of all exhibits of the provided type.
     */
    @JsonIgnore
    fun getExhibits(type: CulturalHeritageObject.Companion.CHOType): List<Exhibit> {
        return getExhibits().filter { e -> e.type == type }
    }
}
