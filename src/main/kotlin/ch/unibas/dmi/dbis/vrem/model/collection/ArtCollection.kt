package ch.unibas.dmi.dbis.vrem.model.collection

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
@Serializable
data class ArtCollection(
        @SerialName("_id")
        val id: String,
        val name: String,
        val exhibits: List<Exhibit> = mutableListOf(),
        val metadata: Map<String, String> = mutableMapOf()
) {

    constructor(id: ObjectId, name: String, exhibits: List<Exhibit>) : this(id.toHexString(), name, exhibits)

}