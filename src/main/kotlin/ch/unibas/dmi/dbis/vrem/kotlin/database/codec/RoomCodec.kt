package ch.unibas.dmi.dbis.vrem.kotlin.database.codec

import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.Wall
import ch.unibas.dmi.dbis.vrem.kotlin.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.model.exhibition.Texture
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
class RoomCodec(registry: CodecRegistry):Codec<Room> {

    companion object{
        const val FIELD_NAME_TEXT = "text"
        const val FIELD_NAME_FLOOR = "floor"
        const val FIELD_NAME_CEILING = "ceiling"
        const val FIELD_NAME_SIZE = "size"
        const val FIELD_NAME_POSITION = "position"
        const val FIELD_NAME_ENTRYPOINT = "entrypoint"
        const val FIELD_NAME_WALLS = "walls"
        const val FIELD_NAME_EXHIBITS = "exhibits"
        const val FIELD_NAME_AMBIENT = "ambient"
    }

    private val exhibitCodec = registry.get(Exhibit::class.java)
    private val wallCodec=registry.get(Wall::class.java)
    private val vectorCodec=registry.get(Vector3f::class.java)

    override fun getEncoderClass(): Class<Room> {
        return Room::class.java
    }

    override fun encode(writer: BsonWriter, value: Room, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        writer.writeString(FIELD_NAME_TEXT, value!!.text)
        writer.writeString(FIELD_NAME_FLOOR, value.floor)
        writer.writeString(FIELD_NAME_CEILING, value.ceiling)
        writer.writeName(FIELD_NAME_SIZE)
        vectorCodec.encode(writer, value.size, encoderContext)
        writer.writeName(FIELD_NAME_POSITION)
        vectorCodec.encode(writer, value.position, encoderContext)
        writer.writeName(FIELD_NAME_ENTRYPOINT)
        vectorCodec.encode(writer, value.entrypoint, encoderContext)
        writer.writeName(FIELD_NAME_WALLS)
        writer.writeStartArray()
        wallCodec.encode(writer, value.getNorth(), encoderContext)
        wallCodec.encode(writer, value.getEast(), encoderContext)
        wallCodec.encode(writer, value.getSouth(), encoderContext)
        wallCodec.encode(writer, value.getWest(), encoderContext)
        writer.writeEndArray()
        writer.writeName(FIELD_NAME_EXHIBITS)
        writer.writeStartArray()
        value.getExhibits().forEach { exhibit ->   exhibitCodec.encode(writer, exhibit, encoderContext) }
        writer.writeEndArray()
        if (value.ambient != null) {
            writer.writeString(FIELD_NAME_AMBIENT, value.ambient)
        }
        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Room {
        reader.readStartDocument()
        var text: String? = null
        var floor: String? = Texture.WOOD1.name
        var ceiling: String? = Texture.CONCRETE.name
        var size: Vector3f? = null
        var position: Vector3f? = null
        var entrypoint: Vector3f? = null
        val walls: MutableList<Wall> = ArrayList()
        val exhibits: MutableList<Exhibit> = ArrayList()
        var ambient: String? = null

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            when (reader.readName()) {
                FIELD_NAME_TEXT -> text = reader.readString()
                FIELD_NAME_FLOOR -> floor = reader.readString()
                FIELD_NAME_CEILING -> ceiling = reader.readString()
                FIELD_NAME_SIZE -> size = vectorCodec.decode(reader, decoderContext)
                FIELD_NAME_POSITION -> position = vectorCodec.decode(reader, decoderContext)
                FIELD_NAME_ENTRYPOINT -> entrypoint = vectorCodec.decode(reader, decoderContext)
                FIELD_NAME_WALLS -> {
                    reader.readStartArray()
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        walls.add(wallCodec.decode(reader, decoderContext))
                    }
                    reader.readEndArray()
                }
                FIELD_NAME_EXHIBITS -> {
                    reader.readStartArray()
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        exhibits.add(exhibitCodec.decode(reader, decoderContext))
                    }
                    reader.readEndArray()
                }
                FIELD_NAME_AMBIENT -> ambient = reader.readString()
                else -> reader.skipValue()
            }
        }
        reader.readEndDocument()
        val room = Room.build(text!!, floor!!, ceiling!!, size!!, position!!, entrypoint!!, ambient, walls)
        for (exhibit in exhibits) {
            room.placeExhibit(exhibit)
        }
        return room
    }


}