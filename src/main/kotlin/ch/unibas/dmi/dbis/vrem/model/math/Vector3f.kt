package ch.unibas.dmi.dbis.vrem.model.math

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Serializable

/**
 * Vector in 3 dimensions with component type float.
 *
 * @author loris.sauter
 */
@Serializable
data class Vector3f(val x:Float, val y:Float, val z:Float) {

    constructor(x:Number, y:Number,z:Number):this(x.toFloat(),y.toFloat(),z.toFloat())

    constructor(x:Number, y:Number):this(x.toFloat(),y.toFloat(), 0f)

    private constructor(x:Float) : this(x,x,x)

    companion object {
        val ORIGIN = Vector3f(0f)
        val UNIT = Vector3f(1f)
        val NaN = Vector3f(Float.NaN)
    }

    override fun equals(other: Any?): Boolean {
        // IntelliJ IDEA generated
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector3f

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        // IntelliJ IDEA generated
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

    @JsonIgnore
    fun isNaN(): Boolean {
        return equals(NaN)
    }

    override fun toString(): String {
        return "Vector3f(x=$x, y=$y, z=$z)"
    }


}