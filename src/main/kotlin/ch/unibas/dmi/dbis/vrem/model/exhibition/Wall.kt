package ch.unibas.dmi.dbis.vrem.model.exhibition

import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import kotlinx.serialization.Serializable

/**
 * Object representation of exhibition room walls.
 *
 * @property color The color of the wall as a vector.
 * @property texture The texture of the wall.
 * @property direction The direction the wall is facing.
 * @property exhibits The exhibits that are hanging on the wall.
 * @constructor
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

    /**
     * Adds an exhibit to the wall if it's not already present.
     *
     * @param exhibit The exhibit to add.
     * @return True if the exhibit was successfully added, false otherwise (also in the case of duplicates).
     */
    fun placeExhibit(exhibit: Exhibit): Boolean {
        if (exhibit.type != CulturalHeritageObject.Companion.CHOType.IMAGE) {
            throw IllegalArgumentException("Only images are to be placed on walls.")
        }
        return if (!exhibits.contains(exhibit)) {
            exhibits.add(exhibit)
        } else {
            false
        }
    }
}
