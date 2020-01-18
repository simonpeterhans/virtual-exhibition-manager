package ch.unibas.dmi.dbis.vrem.kotlin.model.api.request

import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibit

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
data class ExhibitUploadRequest (
        val artCollection:String,
        val exhibit: Exhibit,
        val file:String,
        val fileExtension:String
)