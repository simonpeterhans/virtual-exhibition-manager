package ch.unibas.dmi.dbis.vrem.model.exhibition

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
@Serializable
data class CulturalHertiageObject(
        val id:String,
        val name:String,
        val type:CHOType,
        var path:String,
        val description:String
){

    constructor(name:String, description: String, path: String, type: CHOType):this(ObjectId().toHexString(), name=name, type=type,description = description,path = path)

    companion object{
        enum class CHOType{
            IMAGE, MODEL, VIDEO, MODEL_STRUCTURAL
        }
    }
}