package ch.unibas.dmi.dbis.vrem.database.dao

import ch.unibas.dmi.dbis.vrem.config.DatabaseConfig
import ch.unibas.dmi.dbis.vrem.model.collection.ArtCollection
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

/**
 * Data Access Object (DAO) used for MongoDB access.
 *
 * @property database The MongoDB object to create the DAO for.
 * @constructor
 */
open class VREMDao(protected val database: MongoDatabase) {

    /**
     * MongoDB collection name constants.
     *
     * @constructor
     */
    companion object {
        const val EXHIBITION_COLLECTION = "exhibitions"
        const val CORPUS_COLLECTION = "corpora"

        /**
         * Create data access objects (reader & writer) to access the exhibition/exhibit collections.
         *
         * @param dbConfig The database configuration for the MongoDB instance.
         * @return A pair of VREMReader and VREMWriter to access the database.
         */
        fun getDAOs(dbConfig: DatabaseConfig): Pair<VREMReader, VREMWriter> {
            val dbClient = KMongo.createClient(dbConfig.getConnectionString())
            val db = dbClient.getDatabase(dbConfig.database)
            return VREMReader(db) to VREMWriter(db)
        }
    }

    /**
     * Retrieves the exhibition collection.
     *
     * @return The exhibition collection of the MongoDB instance.
     */
    protected fun getExhibitionCollection(): MongoCollection<Exhibition> {
        return database.getCollection<Exhibition>(EXHIBITION_COLLECTION)
    }

    /**
     * Retrieves the corpora collection.
     *
     * @return The corpora collection of the MongoDB instance.
     */
    protected fun getCorporaCollection(): MongoCollection<ArtCollection> {
        return database.getCollection<ArtCollection>(CORPUS_COLLECTION)
    }

}
