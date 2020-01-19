package ch.unibas.dmi.dbis.vrem.kotlin.database.dao

import ch.unibas.dmi.dbis.vrem.kotlin.model.collection.ArtCollection
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibition
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.getCollection

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
open class VREMDao(protected val database:MongoDatabase) {

    companion object{
        const val EXHIBITION_COLLECTION = "exhibitions"
        const val CORPUS_COLLECTION = "corpora";
    }

    protected fun getExhibitionCollection(): MongoCollection<Exhibition> {
        return  database.getCollection<Exhibition>(EXHIBITION_COLLECTION)
    }

    protected fun getCorporaCollection(): MongoCollection<ArtCollection> {
        return  database.getCollection<ArtCollection>(CORPUS_COLLECTION)
    }
}