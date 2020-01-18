package ch.unibas.dmi.dbis.vrem.kotlin.database.codec

import ch.unibas.dmi.dbis.vrem.kotlin.model.collection.ArtCollection
import ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.kotlin.model.math.Vector3f
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
class VREMCodecProvider : CodecProvider {

    override fun <T : Any?> get(clazz: Class<T>?, registry: CodecRegistry?): Codec<T>? {
        if (clazz == Exhibition::class.java) {
            return (ExhibitionCodec(registry!!) as Codec<T>)
        } else if (clazz == Room::class.java) {
            return (RoomCodec(registry!!) as Codec<T>)
        } else if (clazz == Wall::class.java) {
            return (WallCodec(registry!!) as Codec<T>)
        } else if (clazz == Exhibit::class.java) {
            return (ExhibitCodec(registry!!) as Codec<T>)
        } else if (clazz == CulturalHertiageObject::class.java) {
            return (CulturalHeritageObjectCodec() as Codec<T>)
        } else if (clazz == Vector3f::class.java) {
            return (VectorCodec() as Codec<T>)
        } else if (clazz == ArtCollection::class.java) {
            return (ArtCollectionCodec(registry!!) as Codec<T>)
        }
        return null
    }
}