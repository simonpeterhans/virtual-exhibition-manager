package ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition

import org.bson.types.ObjectId

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
open class CulturalHertiageObject(
        id:ObjectId,
        val name:String,
        val type:CHOType,
        var path:String,
        val description:String
){
    val id:String = id.toHexString()

    constructor(name:String, description: String, path: String, type: CHOType):this(ObjectId(), name=name, type=type,description = description,path = path)

    val metadata = mutableMapOf<String,String>()

    companion object{
        enum class CHOType{
            IMAGE, MODEL, VIDEO
        }
    }
}