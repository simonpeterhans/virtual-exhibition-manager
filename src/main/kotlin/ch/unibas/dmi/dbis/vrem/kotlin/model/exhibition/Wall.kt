package ch.unibas.dmi.dbis.vrem.kotlin.model.exhibition

import ch.unibas.dmi.dbis.vrem.kotlin.model.math.Vector3f
import kotlinx.serialization.Serializable

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
@Serializable
data class Wall(
        val color:Vector3f,
        val texture:String,
        val direction:Direction,
        val exhibits:MutableList<Exhibit> = mutableListOf()
) {

    constructor(direction:Direction, color: Vector3f):this(color,"none", direction)
    constructor(direction:Direction, texture: String):this(Vector3f.UNIT, texture, direction)


    fun placeExhibit(exhibit:Exhibit): Boolean {
        if(exhibit.type != CulturalHertiageObject.Companion.CHOType.IMAGE){
            throw IllegalArgumentException("Only images are to be placed on walls.")
        }
        return if(!exhibits.contains(exhibit)){
            exhibits.add(exhibit)
        }else{
            false
        }
    }

}