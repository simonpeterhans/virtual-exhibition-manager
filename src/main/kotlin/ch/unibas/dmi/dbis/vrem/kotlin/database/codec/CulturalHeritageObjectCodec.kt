package ch.unibas.dmi.dbis.vrem.kotlin.database.codec

import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.CulturalHertiageObject
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.types.ObjectId

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class CulturalHeritageObjectCodec: Codec<CulturalHertiageObject> {

    companion object{
        const val FIELD_NAME_OBJECTID = "_id"
        const val FIELD_NAME_NAME = "name"
        const val FIELD_NAME_DESCRIPTION = "description"
        const val FIELD_NAME_TYPE = "type"
        const val FIELD_NAME_PATH = "path"
    }

    override fun getEncoderClass(): Class<CulturalHertiageObject> {
        return CulturalHertiageObject::class.java
    }

    override fun encode(writer: BsonWriter?, value: CulturalHertiageObject?, encoderContext: EncoderContext?) {
        writer!!.writeStartDocument()
        writer.writeObjectId(FIELD_NAME_OBJECTID, ObjectId(value!!.id))
        writer.writeString(FIELD_NAME_NAME, value.name)
        writer.writeString(FIELD_NAME_DESCRIPTION, value.description)
        writer.writeString(FIELD_NAME_TYPE, value.type.name)
        writer.writeString(FIELD_NAME_PATH, value.path)
        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): CulturalHertiageObject {
        reader!!.readStartDocument()
        var id: ObjectId? = null
        var name: String? = null
        var description: String? = null
        var type: CulturalHertiageObject.Companion.CHOType? = null
        var path: String? = null

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            when (reader.readName()) {
                FIELD_NAME_OBJECTID -> id = reader.readObjectId()
                FIELD_NAME_NAME -> name = reader.readString()
                FIELD_NAME_DESCRIPTION -> description = reader.readString()
                FIELD_NAME_TYPE -> type = CulturalHertiageObject.Companion.CHOType.valueOf(reader.readString())
                FIELD_NAME_PATH -> path = reader.readString()
                else -> reader.skipValue()
            }
        }
        reader.readEndDocument()
        return CulturalHertiageObject(id!!.toHexString(),name!!,type!!,path!!,description!!)
    }
}