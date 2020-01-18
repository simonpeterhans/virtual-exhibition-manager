package ch.unibas.dmi.dbis.vrem.kotlin.database.codec

import ch.unibas.dmi.dbis.vrem.kotlin.model.math.Vector3f
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class VectorCodec : Codec<Vector3f> {

    companion object {
        const val FIELD_NAME_X = "x"
        const val FIELD_NAME_Y = "y"
        const val FIELD_NAME_Z = "z"
    }

    override fun getEncoderClass(): Class<Vector3f> {
        return Vector3f::class.java
    }

    override fun encode(writer: BsonWriter, value: Vector3f, encoderContext: EncoderContext) {
        writer!!.writeStartDocument()
        writer.writeDouble(FIELD_NAME_X, value!!.x.toDouble())
        writer.writeDouble(FIELD_NAME_Y, value.y.toDouble())
        writer.writeDouble(FIELD_NAME_Z, value.z.toDouble())
        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): Vector3f {
        reader!!.readStartDocument()
        var x = 0.0f
        var y = 0.0f
        var z = 0.0f
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            when (reader.readName()) {
                FIELD_NAME_X -> x = reader.readDouble().toFloat()
                FIELD_NAME_Y -> y = reader.readDouble().toFloat()
                FIELD_NAME_Z -> z = reader.readDouble().toFloat()
                else -> reader.skipValue()
            }
        }
        reader.readEndDocument()
        return Vector3f(x, y, z)
    }
}