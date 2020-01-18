package ch.unibas.dmi.dbis.vrem.kotlin.model.api.response

import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibit

/**
 * Response to list exhibits action.
 *
 * Wrapper for a list of exhibits.
 *
 * @author loris.sauter
 */
data class ListExhibitsResponse (val exhibits:List<Exhibit>)