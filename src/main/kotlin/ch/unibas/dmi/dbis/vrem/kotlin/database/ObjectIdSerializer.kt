package ch.unibas.dmi.dbis.vrem.kotlin.database

import com.github.jershell.kbson.BsonFlexibleDecoder
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import org.bson.types.ObjectId

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
@Serializer(forClass= ObjectId::class)
object ObjectIdSerializer : KSerializer<ObjectId>{
    override val descriptor: SerialDescriptor = StringDescriptor.withName("Name") // Not sure what this is exactly for

    override fun serialize(encoder: Encoder, obj: ObjectId) {
        encoder.encodeString(obj.toHexString())
    }

    override fun deserialize(decoder: Decoder): ObjectId {
        return when(decoder){
            is BsonFlexibleDecoder -> {
                val bsonDecoder = decoder as BsonFlexibleDecoder
                bsonDecoder.reader.readObjectId()
            }
            else -> ObjectId(decoder.decodeString())
        }
    }
}