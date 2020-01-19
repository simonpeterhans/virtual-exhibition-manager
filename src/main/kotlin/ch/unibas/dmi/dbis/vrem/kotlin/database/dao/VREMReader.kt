package ch.unibas.dmi.dbis.vrem.kotlin.database.dao

import ch.unibas.dmi.dbis.vrem.kotlin.model.collection.ArtCollection
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.ExhibitionSummary
import com.mongodb.client.MongoDatabase
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.id.toId

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class VREMReader(database: MongoDatabase) : VREMDao(database) {

    fun getExhibition(name: String): Exhibition {
        val col = getExhibitionCollection()
        return col.findOne(Exhibition::name eq name)!!
    }

    fun getExhibition(id: ObjectId): Exhibition {
        val col = getExhibitionCollection()
        return col.findOne { Exhibition::id eq id.toId() }!!
    }


    fun listExhibitions(): List<ExhibitionSummary> {
        val col = getExhibitionCollection()
        return col.find().projection(Exhibition::id, Exhibition::name).map { ExhibitionSummary(it.id.toString(), it.name) }.toMutableList()
    }

    fun listExhibits(): List<Exhibit> {
        val artCollections = database.getCollection<ArtCollection>(CORPUS_COLLECTION)
        return artCollections.distinct(ArtCollection::exhibits).first()!!
    }

    fun listExhibitsFromExhibitions(): List<Exhibition> {
        val exhibitions = database.getCollection(EXHIBITION_COLLECTION, Exhibition::class.java)
        return exhibitions.find().toMutableList()
    }
}