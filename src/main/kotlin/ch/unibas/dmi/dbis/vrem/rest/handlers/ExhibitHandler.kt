package ch.unibas.dmi.dbis.vrem.rest.handlers

import ch.unibas.dmi.dbis.vrem.database.dao.VREMReader
import ch.unibas.dmi.dbis.vrem.database.dao.VREMWriter
import ch.unibas.dmi.dbis.vrem.model.api.request.ExhibitUploadRequest
import ch.unibas.dmi.dbis.vrem.model.api.response.ListExhibitsResponse
import io.javalin.http.Context
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class ExhibitHandler(private val reader: VREMReader, private val writer: VREMWriter, private val docRoot: Path) {

    private val LOGGER = LogManager.getLogger(ExhibitHandler::class.java)

    fun listExhibits(ctx: Context) {
        LOGGER.debug("List exhibits")
        ctx.json(ListExhibitsResponse(reader.listExhibits()))
    }

    fun saveExhibit(ctx: Context) {
        LOGGER.debug("Save exhibit request")
        val exhibitUpload = ctx.body<ExhibitUploadRequest>()

        LOGGER.debug("Save exhibit.id=${exhibitUpload.exhibit.id} and for corpus ${exhibitUpload.artCollection}")
        val path = writer.uploadExhibit(exhibitUpload)

        val base64Img = exhibitUpload.file.split(",")[1]
        val decodedImg = Base64.getDecoder().decode(base64Img.toByteArray(Charsets.UTF_8))

        val dir = docRoot.resolve(exhibitUpload.artCollection)
        if (!Files.exists(dir)) {
            dir.toFile().mkdirs()
        }
        val contentFile = docRoot.resolve(path).toFile()
        contentFile.writeBytes(decodedImg)
        LOGGER.debug("Stored exhibit and content to db and fs (${exhibitUpload.exhibit.id} / $contentFile")
        // return exhibitUpload
        // TODO what to return?
    }

}
