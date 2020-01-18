package ch.unibas.dmi.dbis.vrem.kotlin.database.dao

import ch.unibas.dmi.dbis.vrem.kotlin.database.codec.ArtCollectionCodec
import ch.unibas.dmi.dbis.vrem.kotlin.database.codec.ExhibitionCodec
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.ExhibitionSummary
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import org.bson.types.ObjectId

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class VREMReader(database: MongoDatabase) : VREMDao(database) {

    fun getExhibition(name:String): Exhibition {
        return getExhibition(ExhibitionCodec.FIELD_NAME_NAME, name)
    }

    fun getExhibition(id: ObjectId): Exhibition {
        return getExhibition(ExhibitionCodec.FIELD_NAME_ID, id)
    }

    fun getExhibition(fieldName:String, key:Any): Exhibition {
        val exhibitions = database.getCollection(EXHIBITION_COLLECTION, Exhibition::class.java)
        return exhibitions.find(Filters.eq(fieldName, key)).first()!!
    }

    fun listExhibitions(): List<ExhibitionSummary> {
        val exhibitions = database.getCollection(EXHIBITION_COLLECTION)
        return exhibitions.find().projection(Projections.include(ExhibitionCodec.FIELD_NAME_ID, ExhibitionCodec.FIELD_NAME_NAME)).map { document -> ExhibitionSummary(document.getObjectId(ExhibitionCodec.FIELD_NAME_ID), document.getString(ExhibitionCodec.FIELD_NAME_NAME)) }.toList()
    }

    fun listExhibits(): List<Exhibit> {
        val artCollections = database.getCollection(CORPUS_COLLECTION)
        return artCollections.distinct(ArtCollectionCodec.FIELD_NAME_EXHIBITS, Exhibit::class.java).toList()
    }

    fun listExhibitsFromExhibitions(): List<Exhibition> {
        val exhibitions = database.getCollection(EXHIBITION_COLLECTION, Exhibition::class.java)
        return exhibitions.find().toList()
    }
}