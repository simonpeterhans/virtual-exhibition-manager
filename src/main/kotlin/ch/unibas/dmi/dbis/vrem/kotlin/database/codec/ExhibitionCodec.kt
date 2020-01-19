package ch.unibas.dmi.dbis.vrem.kotlin.database.codec

import ch.unibas.dmi.dbis.vrem.database.codec.ExhibitionCodec
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Room
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId
import org.litote.kmongo.id.toId

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class ExhibitionCodec(registry: CodecRegistry): Codec<Exhibition> {

    companion object{
        const val FIELD_NAME_ID = "_id"
        const val FIELD_NAME_NAME = "name"
        const val FIELD_NAME_DESCRIPTION = "description"
        const val FIELD_NAME_ROOMS = "rooms"
    }

    private val codec = registry.get(Room::class.java)

    override fun getEncoderClass(): Class<Exhibition> {
        return Exhibition::class.java
    }

    override fun encode(writer: BsonWriter?, value: Exhibition?, encoderContext: EncoderContext?) {
        writer!!.writeStartDocument()
        writer.writeObjectId(ExhibitionCodec.FIELD_NAME_ID, ObjectId(value!!.id.toString()))
        writer.writeString(ExhibitionCodec.FIELD_NAME_NAME, value!!.name)
        writer.writeString(ExhibitionCodec.FIELD_NAME_DESCRIPTION, value.description)
        writer.writeName(ExhibitionCodec.FIELD_NAME_ROOMS)
        writer.writeStartArray()
        for (room in value.rooms) {
            codec.encode(writer, room, encoderContext)
        }
        writer.writeEndArray()
        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): Exhibition {
        reader!!.readStartDocument()
        var id: ObjectId? = null
        var name: String? = null
        var description: String? = null
        val rooms: MutableList<Room> = mutableListOf()

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            when (reader.readName()) {
                ExhibitionCodec.FIELD_NAME_ID -> id = reader.readObjectId()
                ExhibitionCodec.FIELD_NAME_NAME -> name = reader.readString()
                ExhibitionCodec.FIELD_NAME_DESCRIPTION -> description = reader.readString()
                ExhibitionCodec.FIELD_NAME_ROOMS -> {
                    reader.readStartArray()
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        rooms.add(codec.decode(reader, decoderContext))
                    }
                    reader.readEndArray()
                }
                else -> reader.skipValue()
            }
        }
        reader.readEndDocument()
        val exhibition = Exhibition(id!!.toId(),name!!, description!!)
        for (room in rooms) {
            exhibition.addRoom(room)
        }
        return exhibition
    }
}