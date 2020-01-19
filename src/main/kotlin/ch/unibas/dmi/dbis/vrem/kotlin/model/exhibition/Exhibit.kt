package ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition

import ch.unibas.dmi.dbis.vrem.kotlin.model.math.Vector3f
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

/**
 *
 */
@Serializable
data class Exhibit( val id: String = ObjectId().toHexString(),
                   val name: String,
                   val description: String,
                   var path: String,
                   val type: CulturalHertiageObject.Companion.CHOType,
                   val size: Vector3f,
                   val position: Vector3f,
                   val audio: String?,
                   val light: Boolean,
                   val metadata:MutableMap<String,String> = mutableMapOf()) {

    companion object{
        fun copy(e:Exhibit): Exhibit {
            return Exhibit(e.name, e.description, e.path, e.type, e.position, e.size, e.audio!!, e.light)
        }
    }

    constructor(id: ObjectId, name: String, description: String, path: String, type: CulturalHertiageObject.Companion.CHOType) : this(id = id.toHexString(), name = name, description = description, path = path, type = type, position = Vector3f.ORIGIN, size = Vector3f.UNIT, audio = null, light = false)
    constructor(id: ObjectId, name: String, description: String, path: String, type: CulturalHertiageObject.Companion.CHOType, position: Vector3f, size: Vector3f) : this(id = id.toHexString(), name = name, description = description, path = path, type = type, position = position, size = size, audio = null, light = false)
    constructor(name: String, description: String, path: String, type: CulturalHertiageObject.Companion.CHOType) : this(id = ObjectId(), name = name, description = description, path = path, type = type)
    constructor(name: String, description: String, path: String, type: CulturalHertiageObject.Companion.CHOType, position: Vector3f, size: Vector3f, audio: String, light: Boolean) : this(ObjectId().toHexString(), name, description, path, type, position, size, audio, light)

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
