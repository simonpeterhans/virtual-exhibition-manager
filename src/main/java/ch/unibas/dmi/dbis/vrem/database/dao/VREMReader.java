package ch.unibas.dmi.dbis.vrem.database.dao;

import ch.unibas.dmi.dbis.vrem.database.codec.ArtCollectionCodec;
import ch.unibas.dmi.dbis.vrem.database.codec.ExhibitionCodec;
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit;
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition;
import ch.unibas.dmi.dbis.vrem.model.exhibition.ExhibitionSummary;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

public class VREMReader extends VREMDao {

    /**
     *
     */
    public VREMReader(MongoDatabase database) {
        super(database);
    }


    public Exhibition getExhibition(String key) {
        return getExhibition(ExhibitionCodec.FIELD_NAME_KEY, key);
    }

    public Exhibition getExhibition(ObjectId id) {
        return getExhibition(ExhibitionCodec.FIELD_NAME_ID, id);
    }

    private Exhibition getExhibition(String fieldName, Object key) {
        final MongoCollection<Exhibition> exhibitions = this.database.getCollection(EXHIBITION_COLLECTION, Exhibition.class);
        return exhibitions.find(Filters.eq(fieldName, key)).first();
    }

    /**
     *
     */
    public List<ExhibitionSummary> listExhibitions() {
        final MongoCollection<Document> exhibitions = database.getCollection(EXHIBITION_COLLECTION);
        final List<ExhibitionSummary> list = new ArrayList<>();
        for (Document document : exhibitions.find().projection(Projections.include(ExhibitionCodec.FIELD_NAME_ID, ExhibitionCodec.FIELD_NAME_KEY))) {
            list.add(new ExhibitionSummary(document.getObjectId(ExhibitionCodec.FIELD_NAME_ID), document.getString(ExhibitionCodec.FIELD_NAME_KEY)));
        }
        return list;
    }

    public List<Exhibit> listExhibits() {
        // Get all Exhibits from all ArtCollections.
        final MongoCollection<Document> artCollections = database.getCollection(CORPUS_COLLECTION);
        final List<Exhibit> list = new ArrayList<>();
        for (Exhibit e : artCollections.distinct(ArtCollectionCodec.FIELD_NAME_EXHIBITS, Exhibit.class)) {
            list.add(e);
        }
        return list;
    }
}


