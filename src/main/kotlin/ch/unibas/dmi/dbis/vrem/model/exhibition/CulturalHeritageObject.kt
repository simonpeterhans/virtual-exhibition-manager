package ch.unibas.dmi.dbis.vrem.model.exhibition

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

/**
 * Cultural heritage object.
 *
 * @property id The ID of the object as a string.
 * @property name The name of the object.
 * @property type The object type as described in the enum.
 * @property path The path to the object.
 * @property description The description of the object.
 * @constructor
 */
@Serializable
data class CulturalHeritageObject(
    val id: String,
    val name: String,
    val type: CHOType,
    var path: String,
    val description: String
) {
    constructor(name: String, description: String, path: String, type: CHOType) : this(
        ObjectId().toHexString(),
        name = name,
        type = type,
        description = description,
        path = path
    )

    companion object {
        /**
         * Types of CHO.
         *
         * @constructor
         */
        enum class CHOType {
            IMAGE, MODEL, VIDEO, MODEL_STRUCTURAL
        }
    }
}
