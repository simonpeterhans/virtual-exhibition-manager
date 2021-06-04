package ch.unibas.dmi.dbis.vrem.model.api.request

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import kotlinx.serialization.Serializable

/**
 * Exhibit upload request.
 *
 * TODO Fixme.
 *
 * @property artCollection The name of the art collection.
 * @property exhibit The exhibit object to upload.
 * @property file The file (path) of the exhibit.
 * @property fileExtension The file extension of the exhibit.
 * @constructor
 */
@Serializable
data class ExhibitUploadRequest(
    val artCollection: String,
    val exhibit: Exhibit,
    val file: String,
    val fileExtension: String
)
