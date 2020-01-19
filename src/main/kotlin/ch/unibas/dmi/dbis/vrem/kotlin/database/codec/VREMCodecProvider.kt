package ch.unibas.dmi.dbis.vrem.kotlin.database.codec

import ch.unibas.dmi.dbis.vrem.kotlin.model.collection.ArtCollection
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.kotlin.model.math.Vector3f
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry

/**
 * Codec provider for mongodb
 * @author loris.sauter
 */
class VREMCodecProvider : CodecProvider {

    override fun <T : Any?> get(clazz: Class<T>?, registry: CodecRegistry?): Codec<T>? {
        return when (clazz) {
            Exhibition::class.java -> (ExhibitionCodec(registry!!) as Codec<T>)
            Room::class.java -> (RoomCodec(registry!!) as Codec<T>)
            Wall::class.java -> (WallCodec(registry!!) as Codec<T>)
            Exhibit::class.java -> (ExhibitCodec(registry!!) as Codec<T>)
            CulturalHertiageObject::class.java -> (CulturalHeritageObjectCodec() as Codec<T>)
            Vector3f::class.java -> (VectorCodec() as Codec<T>)
            ArtCollection::class.java -> (ArtCollectionCodec(registry!!) as Codec<T>)
            else -> null
        }
    }
}