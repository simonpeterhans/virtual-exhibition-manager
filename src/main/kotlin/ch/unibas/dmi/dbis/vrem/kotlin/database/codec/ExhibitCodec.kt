package ch.unibas.dmi.dbis.vrem.kotlin.database.codec

import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.CulturalHertiageObject
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.kotlin.model.math.Vector3f
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
class ExhibitCodec(registry: CodecRegistry) : Codec<Exhibit> {
    companion object {
        const val FIELD_NAME_NAME = "name"
        const val FIELD_NAME_DESCRIPTION = "description"
        const val FIELD_NAME_TYPE = "type"
        const val FIELD_NAME_PATH = "path"
        const val FIELD_NAME_POSITION = "position"
        const val FIELD_NAME_SIZE = "size"
        const val FIELD_NAME_AUDIO = "audio"
        const val FIELD_NAME_LIGHT = "light"
    }

    private val codec = registry.get(Vector3f::class.java)


    override fun getEncoderClass(): Class<Exhibit> {
        return Exhibit::class.java
    }

    override fun encode(writer: BsonWriter?, value: Exhibit?, encoderContext: EncoderContext?) {
        writer!!.writeStartDocument()
        writer.writeString(FIELD_NAME_NAME, value!!.name)
        writer.writeString(FIELD_NAME_DESCRIPTION, value.description)
        writer.writeString(FIELD_NAME_TYPE, value.type.name)
        writer.writeString(FIELD_NAME_PATH, value.path)
        writer.writeName(FIELD_NAME_POSITION)
        codec.encode(writer, value.position, encoderContext)
        writer.writeName(FIELD_NAME_SIZE)
        codec.encode(writer, value.size, encoderContext)
        if (value.audio != null) {
            writer.writeString(FIELD_NAME_AUDIO, value.audio)
        }
        writer.writeBoolean(FIELD_NAME_LIGHT, value.light)
        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): Exhibit {
        reader!!.readStartDocument()
        var id: ObjectId? = null
        var name: String? = null
        var description: String? = null
        var type: CulturalHertiageObject.Companion.CHOType? = null
        var path: String? = null
        var position: Vector3f? = null
        var size: Vector3f? = null
        var guide: String? = null
        var light = false

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            when (reader.readName()) {
                FIELD_NAME_NAME -> name = reader.readString()
                FIELD_NAME_DESCRIPTION -> description = reader.readString()
                FIELD_NAME_TYPE -> type = CulturalHertiageObject.Companion.CHOType.valueOf(reader.readString())
                FIELD_NAME_PATH -> path = reader.readString()
                FIELD_NAME_POSITION -> position = codec.decode(reader, decoderContext)
                FIELD_NAME_SIZE -> size = codec.decode(reader, decoderContext)
                FIELD_NAME_AUDIO -> guide = reader.readString()
                FIELD_NAME_LIGHT -> light = reader.readBoolean()
                else -> reader.skipValue()
            }
        }
        reader.readEndDocument()
        if (id == null) {
            id = ObjectId()
        }
        return Exhibit(id=id.toHexString(),name= name!!,description =  description!!,path =  path!!,type =  type!!,position =  position!!, size = size!!, audio = guide,light =  light)
    }
}