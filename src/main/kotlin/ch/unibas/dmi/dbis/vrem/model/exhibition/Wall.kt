package ch.unibas.dmi.dbis.vrem.model.exhibition

import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import kotlinx.serialization.Serializable

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
@Serializable
data class Wall(
    val color: Vector3f,
    val texture: String = DEFAULT_TEXTURE,
    var direction: Direction,
    val exhibits: MutableList<Exhibit> = mutableListOf()
) {
    companion object {
        const val DEFAULT_TEXTURE = "NONE"
    }

    constructor(direction: Direction, texture: String) : this(Vector3f.UNIT, texture, direction)

    fun placeExhibit(exhibit: Exhibit): Boolean {
        if (exhibit.type != CulturalHertiageObject.Companion.CHOType.IMAGE) {
            throw IllegalArgumentException("Only images are to be placed on walls.")
        }
        return if (!exhibits.contains(exhibit)) {
            exhibits.add(exhibit)
        } else {
            false
        }
    }
}
