package ch.unibas.dmi.dbis.vrem.generation.som

import ch.unibas.dmi.dbis.som.PredictionResult
import ch.unibas.dmi.dbis.som.SOM
import ch.unibas.dmi.dbis.som.grids.Grid
import ch.unibas.dmi.dbis.som.grids.Grid2DSquare
import ch.unibas.dmi.dbis.som.util.DistanceFunction
import ch.unibas.dmi.dbis.som.util.DistanceScalingFunction
import ch.unibas.dmi.dbis.som.util.TimeFunction
import ch.unibas.dmi.dbis.vrem.cineast.client.apis.MetadataApi
import ch.unibas.dmi.dbis.vrem.cineast.client.apis.ObjectApi
import ch.unibas.dmi.dbis.vrem.cineast.client.models.AllFeaturesByCategoryQueryResult
import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.GenerationConfig
import ch.unibas.dmi.dbis.vrem.generation.NodeMap
import ch.unibas.dmi.dbis.vrem.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.rest.handlers.RequestContentHandler
import io.javalin.http.Context
import mu.KotlinLogging
import kotlin.math.sqrt
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class SG(
    val cineastHttp: CineastHttp,
) {

    companion object {
        private const val CINEAST_SOM_CATEGORY = "som" // TODO Make this a parameter (som_semantic and som_visual).
        private const val SEGMENT_SUFFIX = "_1"
        private const val CINEAST_FEATURE_LABEL = "feature"
        private const val CINEAST_ID_LABEL = "id"
    }

    fun getAllIds(): List<String> {
        val ids = ObjectApi().findObjectsAll()

        if (ids.content == null) {
            return arrayListOf()
        }

        return ids.content.map { o -> o.objectId + SEGMENT_SUFFIX }.toCollection(ArrayList())
    }

    fun getAllFeatures(category: String): Map<String, List<Map<String, Any>>> {
        val features: AllFeaturesByCategoryQueryResult = MetadataApi().findAllFeatByCat(category)

        if (features.featureMap == null) {
            return mapOf()
        }

        return features.featureMap
    }

    fun featureListToFeatureData(featureName: String, featureList: List<Map<String, Any>>): DoubleFeatureData {
        val featureData = DoubleFeatureData(featureName)

        for (e in featureList) {
            @Suppress("UNCHECKED_CAST")
            featureData.addSample(
                (e[CINEAST_ID_LABEL] as String).substringBeforeLast(SEGMENT_SUFFIX),
                e[CINEAST_FEATURE_LABEL] as ArrayList<Double>
            )
        }

        return featureData
    }

    fun getFeatureDataFromTableName(tableName: String) {}

    fun getFeatureDataFromCategory(category: String = CINEAST_SOM_CATEGORY): MutableMap<String, DoubleFeatureData> {
        val allFeatures = getAllFeatures(category)

        val featureDataList = mutableMapOf<String, DoubleFeatureData>()

        for (e in allFeatures.entries) {
            featureDataList[e.key] = featureListToFeatureData(e.key, e.value)
        }

        return featureDataList
    }

    fun getInitSigma2D(width: Int, height: Int): Double {
        // Choosing sigma based on the grid size is a good practice.
        return 2.0 * (sqrt(width.toDouble() * width.toDouble() + height.toDouble() * height.toDouble()))
    }

    fun trainSom(features: Array<DoubleArray>): SOM {
        // TODO Set properties via deserialized JSON object from the request.
        val height = 1
        val width = 16
        val depth = features[0].size
        val epochs = 100
        val seed = Random(0)
        val initAlpha = 1.0
        val initSigma = getInitSigma2D(width, height)

        val g = Grid2DSquare(
            height,
            width,
            depth,
            DistanceFunction.euclideanNorm2DTorus(
                intArrayOf(height, width),
                booleanArrayOf(false, true) // Wrap around width, but do not wrap around height.
            ), // Only wrap X.
            rand = seed
        )

        val s = SOM(
            g,
            DistanceScalingFunction.exponentialDecreasing(),
            alpha = TimeFunction.linearDecreasingFactorScaled(initAlpha),
            sigma = TimeFunction.linearDecreasingFactorScaled(initSigma),
            rand = seed
        )

        s.train(features, epochs)

        return s
    }

    fun predictionsToNodeMap(grid: Grid, predictions: ArrayList<PredictionResult>, ids: ArrayList<String>): NodeMap {
        val nodeMap = NodeMap()

        // Add all nodes to the map.
        for (i in grid.nodes.indices) {
            nodeMap.addEmptyNode(i)
        }

        // Add every classified sample to the corresponding node.
        for (i in ids.indices) {
            nodeMap.addClassifiedSample(ids[i], predictions[i])
        }

        // Sort lists.
        for ((k, v) in nodeMap.entries) {
            // Sort ascending which is what we want (smaller distance = more similar).
            nodeMap[k] = v.sortedWith(compareBy(Pair<String, Double>::second)).toCollection(ArrayList())
        }

        return nodeMap
    }

    fun createWalls(dims: IntArray, nodeMap: NodeMap): MutableList<Wall> {
        val ex = Exhibition(name = "Generated Exhibition ${Random.nextInt()}") // TODO Proper naming.

        val exhibits = mutableListOf<Exhibit>()

        // Pick top image for every node and add it.
        // TODO Handle case for empty lists (can this even happen if we have more (distinct) samples than nodes?).
        for (idDistanceList in nodeMap.values) { // Linked hash map, ordered according to node ID.
            val c = idDistanceList[0].first

            val e = Exhibit(name = c, path = c + RequestContentHandler.URL_ID_SUFFIX)

            val imageBytes = cineastHttp.objectRequest(c)

            ExGenUtils.calculateExhibitSize(imageBytes, e, 2f)

            exhibits.add(e)
        }

        // At this point, exhibits is a list of 1 exhibit per node, starting from the top left of the SOM (proceeding row-wise).

        // Create walls (1 for all 4 directions).
        val walls = mutableListOf<Wall>()
        enumValues<Direction>().forEach { e ->
            walls.add(Wall(e, "CONCRETE"))
        }

        // Place exhibits on walls.
        // TODO Explicitly check for 2D/3D.
        for (i in 0 until dims[0]) {
            for (w in 0 until walls.size) {
                for (j in 0 until dims[1] / walls.size) {
                    val exhibit = exhibits.removeFirst()

                    exhibit.position = ExGenUtils.getWallPositionByCoords(i, j)

                    walls[w].exhibits.add(exhibit)
                }
            }
        }

        return walls
    }

    fun createRoomFromWalls(walls: List<Wall>): Room {
        // TODO Parametrize this so we can dynamically add the room to where it actually belongs.
        val room = Room("room01", size = Vector3f(15.5, 15.5, 15.5))

        room.walls.addAll(walls)

        return room
    }

    fun createExhibitionFromRooms(rooms: List<Room>): Exhibition {
        val ex = Exhibition(name = "Generated Exhibition ${Random.nextInt()}")

        ex.rooms.addAll(rooms)

        return ex
    }

    fun genSom(config: GenerationConfig): Exhibition {
        // Check if we have to use all ids or only a sub-selection.
        val allFeatures = if (config.idList.isEmpty()) {
            // Get all features.
            getFeatureDataFromCategory(config.genType.cineastCategory) // Get all features for category.
        } else {
            // Only get features for specific IDs.
            // TODO Implement API call to obtain features for specified IDs.
            getFeatureDataFromCategory(config.genType.cineastCategory)
        }

        // Pick the feature type we actually want.
        val features = allFeatures[config.genType.tableName] ?: return Exhibition(name = "Empty Exhibition")

        // Get all values as 2D array.
        val data = features.valuesTo2DArray() // Same order as the IDs.
        val ids = features.getSortedIds()

        // TODO Normalize data if necessary.

        // Train SOM.
        val som = trainSom(data) // TODO Add parameters from request (deserialize from JSON to object).

        // Predict data.
        val predictions = som.predict(data)

        // Create node map.
        val nodeMap = predictionsToNodeMap(som.grid, predictions, ids)

        // Create exhibitions depending on settings (we could create the sub-rooms right away as well).
        // TODO Create a model for generated exhibitions.
        // TODO Allow to return just a room instead of an entire exhibition.
        val walls = createWalls(som.grid.dims, nodeMap)

        val rooms = createRoomFromWalls(walls)

        val ex = createExhibitionFromRooms(arrayListOf(rooms))

        return ex
    }

    fun generateSom(ctx: Context) {
        // TODO Extract this function into a separate class (GenHandler) that creates instances of this class.
        val config = ctx.body<GenerationConfig>()

        val ex = genSom(config)

        ctx.json(ex)
    }

}
