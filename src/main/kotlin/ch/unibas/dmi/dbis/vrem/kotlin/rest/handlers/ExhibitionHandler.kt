package ch.unibas.dmi.dbis.vrem.kotlin.rest.handlers

import ch.unibas.dmi.dbis.vrem.kotlin.database.dao.VREMReader
import ch.unibas.dmi.dbis.vrem.kotlin.database.dao.VREMWriter
import ch.unibas.dmi.dbis.vrem.kotlin.model.api.response.ErrorResponse
import ch.unibas.dmi.dbis.vrem.kotlin.model.api.response.ListExhibitionsResponse
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.kotlin.rest.APIEndpoint
import io.javalin.http.Context
import org.apache.logging.log4j.LogManager
import org.bson.types.ObjectId

/**
 * Handler for exhibition related things
 * @author loris.sauter
 */
class ExhibitionHandler(private val reader: VREMReader, private val writer: VREMWriter) {

    private val LOGGER = LogManager.getLogger(ExhibitionHandler::class.java)

    companion object {
        const val PARAM_KEY_ID = ":id"
        const val PARAM_KEY_NAME = ":name"
    }

    fun listExhibitions(ctx: Context) {
        LOGGER.debug("List exhibitions")
        ctx.result(APIEndpoint.objectMapper.writeValueAsString(ListExhibitionsResponse(reader.listExhibitions())))
    }

    fun loadExhibitionById(ctx: Context) {
        val id = ctx.pathParam(PARAM_KEY_ID)
        LOGGER.debug("Load exhibition by id=$id")
        ctx.result(APIEndpoint.objectMapper.writeValueAsString(reader.getExhibition(ObjectId(id))))
    }

    fun loadExhibitionByName(ctx: Context) {
        val name = ctx.pathParam(PARAM_KEY_NAME)
        LOGGER.debug("Load exhibition by name=$name")
        ctx.result(APIEndpoint.objectMapper.writeValueAsString(reader.getExhibition(name)))
    }

    fun saveExhibition(ctx: Context) {
        LOGGER.debug("Save exhibition request")
        val exhibition = APIEndpoint.objectMapper.readValue(ctx.body(), Exhibition::class.java)
        LOGGER.debug("Save exhibition.id=${exhibition.id}")
        if(writer.saveExhibition(exhibition)){
            LOGGER.debug("Successfully saved exhibition.id=${exhibition.id}")
            ctx.result(APIEndpoint.objectMapper.writeValueAsString(exhibition))
        }else{
            LOGGER.debug("Could not save exhibition.id=${exhibition.id} for unknown reasons")
            ctx.status(500).result(APIEndpoint.objectMapper.writeValueAsString(ErrorResponse("Could not save the exhibition (id=${exhibition.id}")))
        }
    }
}