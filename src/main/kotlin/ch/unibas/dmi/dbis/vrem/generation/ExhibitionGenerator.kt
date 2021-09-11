package ch.unibas.dmi.dbis.vrem.generation

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.model.exhibition.MetadataType
import java.time.LocalDateTime

open class ExhibitionGenerator {

    fun getTextSuffix(): LocalDateTime? = LocalDateTime.now()

    private val exhibitionText: String = "Generated Exhibition"

    open fun genExhibition(name: String = exhibitionText + " " + getTextSuffix()): Exhibition {
        // Generic exhibition object without rooms or anything.
        val ex = Exhibition(name = name)
        ex.metadata[MetadataType.GENERATED.key] = true.toString()

        return ex
    }

}
