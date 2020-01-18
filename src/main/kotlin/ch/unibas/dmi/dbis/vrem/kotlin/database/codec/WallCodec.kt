package ch.unibas.dmi.dbis.vrem.kotlin.database.codec

import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Direction
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Wall
import ch.unibas.dmi.dbis.vrem.kotlin.model.math.Vector3f
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import java.util.*

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class WallCodec(registry:CodecRegistry):Codec<Wall> {

    private val vectorCodec = registry.get(Vector3f::class.java)
    private val exhibitCodec = registry.get(Exhibit::class.java)

    companion object{
        const val FIELD_NAME_DIRECTION = "direction"
        const val FIELD_NAME_TEXTURE = "texture"
        const val FIELD_NAME_COLOR = "color"
        const val FIELD_NAME_EXHIBITS = "exhibits"
    }

    override fun getEncoderClass(): Class<Wall> {
        return Wall::class.java
    }

    override fun encode(writer: BsonWriter, value: Wall, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        writer.writeString(FIELD_NAME_DIRECTION, value.direction.name)
        writer.writeString(FIELD_NAME_TEXTURE, value.texture)
        writer.writeName(FIELD_NAME_COLOR)
        vectorCodec.encode(writer, value.color, encoderContext)
        writer.writeName(FIELD_NAME_EXHIBITS)
        writer.writeStartArray()
        value.getExhibits().forEach { exhibit -> exhibitCodec.encode(writer, exhibit, encoderContext) }
        writer.writeEndArray()
        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Wall {
        reader.readStartDocument()
        var texture: String? = null
        var direction: Direction? = null
        val position: Vector3f? = null
        var color: Vector3f? = null
        val exhibits: MutableList<Exhibit> = ArrayList()
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            when (reader.readName()) {
                FIELD_NAME_DIRECTION -> direction = Direction.valueOf(reader.readString())
                FIELD_NAME_TEXTURE ->  //texture = Texture.valueOf(reader.readString());
                    texture = reader.readString()
                FIELD_NAME_COLOR -> color = vectorCodec.decode(reader, decoderContext)
                FIELD_NAME_EXHIBITS -> {
                    reader.readStartArray()
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        exhibits.add(exhibitCodec.decode(reader, decoderContext))
                    }
                    reader.readEndArray()
                }
                else -> reader.skipValue()
            }
        }
        reader.readEndDocument()

        /* Make final assembly. */
        val wall: Wall
        wall = if (texture == null) {
            Wall(direction = direction!!, color = color!!)
        } else {
            Wall(direction!!, texture)
        }
        for (exhibit in exhibits) {
            wall.placeExhibit(exhibit)
        }
        return wall
    }
}