package ch.unibas.dmi.dbis.vrem.database.codec;

import ch.unibas.dmi.dbis.vrem.model.collection.ArtCollection;
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit;
import java.util.LinkedList;
import java.util.List;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

public class ArtCollectionCodec implements Codec<ArtCollection> {

    public static final String FIELD_NAME_OBJECTID = "_id";
    public static final String FIELD_NAME_NAME = "name";
    public static final String FIELD_NAME_EXHIBITS = "exhibits";

    private final Codec<Exhibit> codec;

    /**
     *
     */
    public ArtCollectionCodec(CodecRegistry registry) {
        this.codec = registry.get(Exhibit.class);
    }

    @Override
    public ArtCollection decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        ObjectId id = null;
        String name = null;
        List<Exhibit> exhibits = new LinkedList<>();

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            switch (reader.readName()) {
                case FIELD_NAME_OBJECTID:
                    id = reader.readObjectId();
                case FIELD_NAME_NAME:
                    name = reader.readString();
                    break;
                case FIELD_NAME_EXHIBITS:
                    reader.readStartArray();
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        exhibits.add(this.codec.decode(reader, decoderContext));
                    }
                    reader.readEndArray();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.readEndDocument();
        if (id == null) {
            id = new ObjectId();
        }
        return new ArtCollection(id, name, exhibits);
    }

    @Override
    public void encode(BsonWriter writer, ArtCollection value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString(FIELD_NAME_OBJECTID, value.id);
        writer.writeString(FIELD_NAME_NAME, value.name);
        writer.writeStartArray();
        for (Exhibit exhibit : value.getExhibits()) {
            this.codec.encode(writer, exhibit, encoderContext);
        }
        writer.writeEndArray();
        writer.writeEndDocument();
    }

    @Override
    public Class<ArtCollection> getEncoderClass() {
        return ArtCollection.class;
    }
}
