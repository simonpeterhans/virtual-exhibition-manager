package ch.unibas.dmi.dbis.vrem.kotlin.rest.handlers

import ch.unibas.dmi.dbis.vrem.kotlin.database.dao.VREMReader
import ch.unibas.dmi.dbis.vrem.kotlin.database.dao.VREMWriter
import ch.unibas.dmi.dbis.vrem.kotlin.model.api.response.ErrorResponse
import ch.unibas.dmi.dbis.vrem.kotlin.model.api.response.ListExhibitionsResponse
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.kotlin.rest.APIEndpoint
import io.javalin.http.Context
import org.bson.types.ObjectId

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class ExhibitionHandler(private val reader: VREMReader, private val writer: VREMWriter) {

    companion object {
        const val PARAM_KEY_ID = ":id"
        const val PARAM_KEY_NAME = ":name"
    }

    fun listExhibitions(ctx: Context) {
        ctx.result(APIEndpoint.objectMapper.writeValueAsString(ListExhibitionsResponse(reader.listExhibitions())))
    }

    fun loadExhibitionById(ctx: Context) {
        ctx.result(APIEndpoint.objectMapper.writeValueAsString(reader.getExhibition(ObjectId(ctx.pathParam(PARAM_KEY_ID)))))
    }

    fun loadExhibitionByName(ctx: Context) {
        ctx.result(APIEndpoint.objectMapper.writeValueAsString(reader.getExhibition(ctx.pathParam(PARAM_KEY_NAME))))
    }

    fun saveExhibition(ctx: Context) {
        val exhibition = APIEndpoint.objectMapper.readValue(ctx.body(), Exhibition::class.java)
        if(writer.saveExhibition(exhibition)){
            ctx.result(APIEndpoint.objectMapper.writeValueAsString(exhibition))
        }else{
            ctx.status(500).result(APIEndpoint.objectMapper.writeValueAsString(ErrorResponse("Could not save the exhibition (id=${exhibition.id}")))
        }
    }
}