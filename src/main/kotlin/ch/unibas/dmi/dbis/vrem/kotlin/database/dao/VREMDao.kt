package ch.unibas.dmi.dbis.vrem.kotlin.database.dao

import com.mongodb.client.MongoDatabase

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
open class VREMDao(protected val database:MongoDatabase) {

    companion object{
        const val EXHIBITION_COLLECTION = "exhibitions"
        const val CORPUS_COLLECTION = "corpora";
    }
}