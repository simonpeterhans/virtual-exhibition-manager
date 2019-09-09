package ch.unibas.dmi.dbis.vrem.database.dao;

import com.mongodb.client.MongoDatabase;

public class VREMDao {

    public final static String EXHIBITION_COLLECTION = "exhibitions";
    public final static String CORPUS_COLLECTION = "corpora";


    protected final MongoDatabase database;

    /**
     *
     */
    public VREMDao(MongoDatabase database) {
        this.database = database;
    }
}
