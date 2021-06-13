package ch.unibas.dmi.dbis.vrem.model.exhibition

import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 * Exhibit object.
 *
 * @property id The ID of the exhibit (defaulted to the object's ID as hex string).
 * @property name The name of the exhibit.
 * @property description The description of the exhibit.
 * @property path The path of the exhibit.
 * @property type The type of the exhibit from the CHO type enum.
 * @property size The size of the exhibit as a vector.
 * @property position The position of the exhibit as a vector.
 * @property audio The name of the audio for the exhibit as a string.
 * @property light Whether the exhibit has light or not.
 * @property metadata The mapping for the metadata attributed of the object.
 * @constructor
 */
@Serializable
data class Exhibit(
    @Contextual
    @SerialName("_id")
    @JsonNames("id", "_id")
    val id: Id<Exhibit> = newId(),
    var name: String,
    var description: String = "",
    var path: String = "",
    val type: CulturalHeritageObject.Companion.CHOType = DEFAULT_TYPE,
    var size: Vector3f = DEFAULT_SIZE,
    var position: Vector3f = DEFAULT_POSITION,
    val audio: String? = null,
    val light: Boolean = false,
    val metadata: MutableMap<String, String> = mutableMapOf()
) {
    constructor(name: String, path: String, choType: CulturalHeritageObject.Companion.CHOType) : this(
        name = name,
        path = path,
        type = choType
    )

    companion object {
        /**
         * Creates a copy of an exhibit and returns the newly created object.
         *
         * @param e The exhibit to copy.
         * @return The newly created copy of the exhibit.
         */
        fun copy(e: Exhibit): Exhibit {
            return Exhibit(
                newId(),
                e.name,
                e.description,
                e.path,
                e.type,
                e.position,
                e.size,
                e.audio!!,
                e.light
            )
        }

        val DEFAULT_SIZE = Vector3f.UNIT
        val DEFAULT_POSITION = Vector3f.ORIGIN
        val DEFAULT_TYPE = CulturalHeritageObject.Companion.CHOType.IMAGE
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Exhibit

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (path != other.path) return false
        if (type != other.type) return false
        if (size != other.size) return false
        if (position != other.position) return false
        if (audio != other.audio) return false
        if (light != other.light) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + (audio?.hashCode() ?: 0)
        result = 31 * result + light.hashCode()
        result = 31 * result + metadata.hashCode()
        return result
    }

    override fun toString(): String {
        return "Exhibit(id=$id, name='$name', description='$description', path='$path', type=$type, size=$size, position=$position, audio=$audio, light=$light, metadata=$metadata)"
    }

}
