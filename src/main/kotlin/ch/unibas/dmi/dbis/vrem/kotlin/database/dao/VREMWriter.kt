package ch.unibas.dmi.dbis.vrem.kotlin.database.dao

import ch.unibas.dmi.dbis.vrem.kotlin.database.codec.ArtCollectionCodec
import ch.unibas.dmi.dbis.vrem.kotlin.database.codec.ExhibitionCodec
import ch.unibas.dmi.dbis.vrem.kotlin.model.api.request.ExhibitUploadRequest
import ch.unibas.dmi.dbis.vrem.kotlin.model.collection.ArtCollection
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibition
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.apache.logging.log4j.LogManager
import org.bson.types.ObjectId

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class VREMWriter(database: MongoDatabase) : VREMDao(database) {

    private val LOGGER = LogManager.getLogger(VREMWriter::class.java)

    fun saveExhibition(exhibition: Exhibition): Boolean {
        val collection = database.getCollection(EXHIBITION_COLLECTION, Exhibition::class.java)
        val result = collection.replaceOne(Filters.eq(ExhibitionCodec.FIELD_NAME_ID, exhibition.id), exhibition)
        if (result.matchedCount == 0L) {
            collection.insertOne(exhibition)
        }
        return result.modifiedCount != 0L
    }

    fun uploadExhibit(exhibitUploadRequest: ExhibitUploadRequest): String {
        val collection = database.getCollection(CORPUS_COLLECTION, ArtCollection::class.java)

        val toAdd = Exhibit.copy(exhibitUploadRequest.exhibit)
        toAdd.path = "${exhibitUploadRequest.artCollection}/${toAdd.id}.${exhibitUploadRequest.fileExtension}"

        if (collection.countDocuments() == 0L) {
            LOGGER.debug("There is no previous document in ${CORPUS_COLLECTION}. We're adding one")
            val artCollection = ArtCollection(ObjectId(), "DefaultCorpus", listOf(toAdd))
            collection.insertOne(artCollection)
            LOGGER.info("Successfully created the default corpus 'DefaultCorpus' and added an exhibit to it")
        } else {
            val result = collection.updateOne(Filters.eq(ArtCollectionCodec.FIELD_NAME_NAME, exhibitUploadRequest.artCollection), Updates.push(ArtCollectionCodec.FIELD_NAME_EXHIBITS, toAdd))
            if (result.matchedCount == 0L) {
                LOGGER.error("Could not update the corpus ${exhibitUploadRequest.artCollection}")
            } else {
                LOGGER.info("Updated the corpus ${exhibitUploadRequest.artCollection}")
            }
        }
        return toAdd.path
    }

    fun deleteExhibition(name: String) {
        database.getCollection(EXHIBITION_COLLECTION).deleteMany(Filters.eq(ExhibitionCodec.FIELD_NAME_NAME, name))
    }
}