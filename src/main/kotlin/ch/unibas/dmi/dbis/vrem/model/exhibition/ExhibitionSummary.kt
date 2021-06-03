package ch.unibas.dmi.dbis.vrem.model.exhibition

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

/**
 * Tuple of objectId and name of an exhibition.
 * @author loris.sauter
 */
@Serializable
data class ExhibitionSummary(val objectId: String, val name: String) {
    constructor(id: ObjectId, name: String) : this(id.toHexString(), name)
}
