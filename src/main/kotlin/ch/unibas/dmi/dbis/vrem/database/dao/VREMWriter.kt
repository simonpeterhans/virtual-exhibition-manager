package ch.unibas.dmi.dbis.vrem.database.dao

import ch.unibas.dmi.dbis.vrem.model.api.request.ExhibitUploadRequest
import ch.unibas.dmi.dbis.vrem.model.collection.ArtCollection
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import com.mongodb.client.MongoDatabase
import org.apache.logging.log4j.LogManager
import org.litote.kmongo.eq
import org.litote.kmongo.push
import org.litote.kmongo.replaceOneById

/**
 * MongoDB writer for VREM.
 *
 * @constructor
 *
 * @param database The MongoDB object to create the writer for.
 */
class VREMWriter(database: MongoDatabase) : VREMDao(database) {

    companion object {
        private val LOGGER = LogManager.getLogger(VREMWriter::class.java)
    }

    /**
     * Stores an exhibition in the exhibition collection.
     *
     * @param exhibition The exhibition to store.
     * @return True if any changes were made, false otherwise.
     */
    fun saveExhibition(exhibition: Exhibition): Boolean {
        val collection = getExhibitionCollection()
        val result = collection.replaceOneById(exhibition.id, exhibition)
        if (result.matchedCount == 0L) {
            collection.insertOne(exhibition)
        }
        return result.modifiedCount != 0L
    }

    /**
     * Store a single exhibit in the exhibit corpus.
     * Creates the collection if it doesn't exist.
     *
     * TODO Fix this after refactoring collection storage handling.
     *
     * @param exhibitUploadRequest A request for the object to upload.
     * @return The path of the uploaded exhibit.
     */
    fun uploadExhibit(exhibitUploadRequest: ExhibitUploadRequest): String {
        val collection = getCorporaCollection()

        val toAdd = Exhibit.copy(exhibitUploadRequest.exhibit)
        toAdd.path = "${exhibitUploadRequest.artCollection}/${toAdd.id}.${exhibitUploadRequest.fileExtension}"

        if (collection.countDocuments() == 0L) {
            LOGGER.debug("There is no previous document in ${CORPUS_COLLECTION}. We're adding one")
            val artCollection = ArtCollection(name = "DefaultCorpus", exhibits = listOf(toAdd))
            collection.insertOne(artCollection)
            LOGGER.info("Successfully created the default corpus 'DefaultCorpus' and added an exhibit to it")
        } else {
            val result = collection.updateOne(
                ArtCollection::name eq exhibitUploadRequest.artCollection,
                push(ArtCollection::exhibits, toAdd)
            )
            if (result.matchedCount == 0L) {
                LOGGER.error("Could not update the corpus ${exhibitUploadRequest.artCollection}")
            } else {
                LOGGER.info("Updated the corpus ${exhibitUploadRequest.artCollection}")
            }
        }
        return toAdd.path
    }

    /**
     * Removes an exhibition from the collection by name.
     *
     * @param name The name of the exhibition to remove.
     */
    fun deleteExhibition(name: String) {
        getExhibitionCollection().deleteMany(Exhibition::name eq name)
    }

}
