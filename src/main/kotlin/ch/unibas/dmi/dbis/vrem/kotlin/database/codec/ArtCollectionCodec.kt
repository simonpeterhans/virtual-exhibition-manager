package ch.unibas.dmi.dbis.vrem.kotlin.database.codec

import ch.unibas.dmi.dbis.vrem.database.codec.ArtCollectionCodec
import ch.unibas.dmi.dbis.vrem.kotlin.model.collection.ArtCollection
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibit
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class ArtCollectionCodec(registry: CodecRegistry): Codec<ArtCollection> {

    private val codec = registry.get(Exhibit::class.java)

    companion object{
        const val FIELD_NAME_OBJECTID = "_id"
        const val FIELD_NAME_NAME = "name"
        const val FIELD_NAME_EXHIBITS = "exhibits"
    }

    override fun getEncoderClass(): Class<ArtCollection> {
        return ArtCollection::class.java
    }

    override fun encode(writer: BsonWriter?, value: ArtCollection?, encoderContext: EncoderContext?) {
        writer!!.writeStartDocument()
        writer.writeString(ArtCollectionCodec.FIELD_NAME_OBJECTID, value!!.id)
        writer.writeString(ArtCollectionCodec.FIELD_NAME_NAME, value.name)
        writer.writeName(ArtCollectionCodec.FIELD_NAME_EXHIBITS)
        writer.writeStartArray()
        for (exhibit in value.exhibits) {
            codec.encode(writer, exhibit, encoderContext)
        }
        writer.writeEndArray()
        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): ArtCollection {
        reader!!.readStartDocument()
        var id: ObjectId? = null
        var name: String? = null
        val exhibits: MutableList<Exhibit> = mutableListOf()

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            when (reader.readName()) {
                ArtCollectionCodec.FIELD_NAME_OBJECTID -> {
                    id = reader.readObjectId()
                    name = reader.readString()
                }
                ArtCollectionCodec.FIELD_NAME_NAME -> name = reader.readString()
                ArtCollectionCodec.FIELD_NAME_EXHIBITS -> {
                    reader.readStartArray()
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        exhibits.add(codec.decode(reader, decoderContext))
                    }
                    reader.readEndArray()
                }
                else -> reader.skipValue()
            }
        }
        reader.readEndDocument()
        if (id == null) {
            id = ObjectId()
        }
        return ArtCollection(id,name!!,exhibits)
    }
}