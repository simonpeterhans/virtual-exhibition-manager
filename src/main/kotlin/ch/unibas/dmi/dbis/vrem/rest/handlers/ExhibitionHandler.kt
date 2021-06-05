package ch.unibas.dmi.dbis.vrem.rest.handlers

import ch.unibas.dmi.dbis.vrem.database.dao.VREMReader
import ch.unibas.dmi.dbis.vrem.database.dao.VREMWriter
import ch.unibas.dmi.dbis.vrem.model.api.response.ErrorResponse
import ch.unibas.dmi.dbis.vrem.model.api.response.ListExhibitionsResponse
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import io.javalin.http.Context
import org.apache.logging.log4j.LogManager
import org.bson.types.ObjectId

/**
 * Exhibition handler for API requests.
 *
 * @property reader The VREM Reader instance to use for MongoDB access.
 * @property writer The VREM Writer instance to use for MongoDB access.
 * @constructor
 */
class ExhibitionHandler(private val reader: VREMReader, private val writer: VREMWriter) {

    private val LOGGER = LogManager.getLogger(ExhibitionHandler::class.java)

    companion object {
        const val PARAM_KEY_ID = ":id"
        const val PARAM_KEY_NAME = ":name"
    }

    /**
     * Lists all exhibitions.
     *
     * @param ctx The Javalin request context.
     */
    fun listExhibitions(ctx: Context) {
        LOGGER.debug("List exhibitions")
        ctx.json(ListExhibitionsResponse(reader.listExhibitions()))
    }

    /**
     * Loads exhibition by ID from the collection of exhibitions.
     *
     * @param ctx The Javalin request context.
     */
    fun loadExhibitionById(ctx: Context) {
        val id = ctx.pathParam(PARAM_KEY_ID)
        LOGGER.debug("Load exhibition by id=$id")
        ctx.json(reader.getExhibition(ObjectId(id)))
    }

    /**
     * Loads exhibition by name from the collection of exhibitions.
     *
     * @param ctx The Javalin request context.
     */
    fun loadExhibitionByName(ctx: Context) {
        val name = ctx.pathParam(PARAM_KEY_NAME)
        LOGGER.debug("Load exhibition by name=$name")
        ctx.json(reader.getExhibition(name))
    }

    /**
     * Stores an exhibition in MongoDB.
     *
     * @param ctx The Javalin request context.
     */
    fun saveExhibition(ctx: Context) {
        LOGGER.debug("Save exhibition request")
        val exhibition = ctx.body<Exhibition>()
        LOGGER.debug("Save exhibition.id=${exhibition.id}")
        if (writer.saveExhibition(exhibition)) {
            LOGGER.debug("Successfully saved exhibition.id=${exhibition.id}")
            ctx.json(exhibition)
        } else {
            LOGGER.debug("Could not save exhibition.id=${exhibition.id} for unknown reasons")
            ctx.status(500).json(ErrorResponse("Could not save the exhibition (id=${exhibition.id}"))
        }
    }

}
