package ch.unibas.dmi.dbis.vrem.model.collection

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

/**
 * Art collection object.
 *
 * @property id The ID of the art collection (as stored in MongoDB).
 * @property name The name of the collection.
 * @property exhibits The exhibits present in the collection.
 * @property metadata Mapping of metadata attributes to descriptions for the collection.
 * @constructor
 */
@Serializable
data class ArtCollection(
    @Contextual
    @SerialName("_id")
    val id: String = ObjectId().toString(),
    val name: String,
    val exhibits: List<Exhibit> = mutableListOf(),
    val metadata: Map<String, String> = mutableMapOf()
)
