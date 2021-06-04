package ch.unibas.dmi.dbis.vrem.model.exhibition

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

/**
 * Tuple of objectId and name of an exhibition.
 * @author loris.sauter
 */

/**
 * Exhibition summary in the form of a tuple of object ID and the name of the exhibition.
 *
 * @property objectId The ID of the exhibition.
 * @property name The name of the exhibition.
 * @constructor
 */
@Serializable
data class ExhibitionSummary(val objectId: String, val name: String) {
    constructor(id: ObjectId, name: String) : this(id.toHexString(), name)
}
