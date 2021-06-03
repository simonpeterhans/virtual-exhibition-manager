package ch.unibas.dmi.dbis.vrem.model.api.response

import ch.unibas.dmi.dbis.vrem.model.exhibition.ExhibitionSummary
import kotlinx.serialization.Serializable

/**
 * Response to list exhibitions action.
 *
 * Wrapper for a list of exhibitions
 *
 * @author loris.sauter
 */
@Serializable
data class ListExhibitionsResponse(val exhibitions: List<ExhibitionSummary>)
