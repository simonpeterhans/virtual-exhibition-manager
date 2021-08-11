package ch.unibas.dmi.dbis.vrem.generation.som

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class ExGenUtils {

    companion object {

        fun calculateExhibitSize(exhibitFile: ByteArray, exhibit: Exhibit, defaultLongSide: Float) {
            // Load image as stream so we don't have to load the entire thing.
            val imageStream = ImageIO.createImageInputStream(ByteArrayInputStream(exhibitFile))

            val reader = ImageIO.getImageReaders(imageStream).next()
            reader.input = imageStream

            // Get width and height.
            val imageWidth = reader.getWidth(0)
            val imageHeight = reader.getHeight(0)

            // Close stream.
            imageStream.close()

            // Calculate aspect ratio and adjust width/height accordingly.
            val aspectRatio = imageHeight.toFloat() / imageWidth

            var width = defaultLongSide
            var height = defaultLongSide

            if (imageWidth > imageHeight) {
                height = (aspectRatio * (defaultLongSide * 100f)) / 100f // Convert to cm for precision.
            } else {
                width = ((defaultLongSide * 100f) / aspectRatio) / 100f // Convert to cm for precision.
            }

            // Update width/height.
            exhibit.size = Vector3f(width, height)
        }

        // TODO SOM coords: 0/0 top left; Wall coords: 0/0 bottom left!
        fun getWallPositionByCoords(
            row: Int,
            column: Int,
            padding: Double = 1.5, // Towards other exhibits and walls.
            longerSideLength: Double = 2.0
        ): Vector3f {
            return Vector3f(
                (longerSideLength + padding) * column + 0.5 * longerSideLength + padding,
                (longerSideLength + padding) * row + 0.5 * longerSideLength + padding,
                0.0
            )
        }
    }

}
