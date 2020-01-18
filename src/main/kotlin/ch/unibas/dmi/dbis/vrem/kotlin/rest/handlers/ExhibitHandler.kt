package ch.unibas.dmi.dbis.vrem.kotlin.rest.handlers

import ch.unibas.dmi.dbis.vrem.kotlin.database.dao.VREMReader
import ch.unibas.dmi.dbis.vrem.kotlin.database.dao.VREMWriter
import ch.unibas.dmi.dbis.vrem.kotlin.model.api.request.ExhibitUploadRequest
import ch.unibas.dmi.dbis.vrem.kotlin.model.api.response.ListExhibitsResponse
import ch.unibas.dmi.dbis.vrem.kotlin.rest.APIEndpoint
import io.javalin.http.Context
import kotlinx.serialization.toUtf8Bytes
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class ExhibitHandler(private val reader: VREMReader, private val writer: VREMWriter, private val docRoot: Path) {

    fun listExhibits(ctx: Context){
        ctx.result(APIEndpoint.objectMapper.writeValueAsString(ListExhibitsResponse(reader.listExhibits())))
    }

    fun saveExhibit(ctx:Context): ExhibitUploadRequest? {
        val exhibitUpload = APIEndpoint.objectMapper.readValue(ctx.body(), ExhibitUploadRequest::class.java)

        val path = writer.uploadExhibit(exhibitUpload)

        val base64Img = exhibitUpload.file.split(",")[1]
        val decodedImg = Base64.getDecoder().decode(base64Img.toUtf8Bytes())

        val dir = docRoot.resolve(exhibitUpload.artCollection)
        if(!Files.exists(dir)){
            dir.toFile().mkdirs()
        }
        docRoot.resolve(path).toFile().writeBytes(decodedImg)
        return exhibitUpload
    }

}
