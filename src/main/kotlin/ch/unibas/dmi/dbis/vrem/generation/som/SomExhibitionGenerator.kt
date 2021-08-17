package ch.unibas.dmi.dbis.vrem.generation.som

import ch.unibas.dmi.dbis.som.PredictionResult
import ch.unibas.dmi.dbis.som.SOM
import ch.unibas.dmi.dbis.som.grids.Grid
import ch.unibas.dmi.dbis.som.grids.Grid2DSquare
import ch.unibas.dmi.dbis.som.util.DistanceFunction
import ch.unibas.dmi.dbis.som.util.DistanceScalingFunction
import ch.unibas.dmi.dbis.som.util.TimeFunction
import ch.unibas.dmi.dbis.vrem.generation.*
import ch.unibas.dmi.dbis.vrem.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.rest.handlers.RequestContentHandler
import mu.KotlinLogging
import kotlin.math.sqrt
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class SomExhibitionGenerator(
    val genConfig: GenerationConfig,
    val cineastHttp: CineastHttp
) {

    fun getInitSigma2D(width: Int, height: Int): Double {
        // Choosing sigma based on the grid size is a good practice.
        return 2.0 * (sqrt(width.toDouble() * width.toDouble() + height.toDouble() * height.toDouble()))
    }

    fun trainSom(features: Array<DoubleArray>): SOM {
        val height = genConfig.height
        val width = genConfig.width
        val depth = features[0].size
        val epochs = 100 // TODO Calculate this dynamically?
        val seed = Random(genConfig.seed)
        val initAlpha = 1.0
        val initSigma = getInitSigma2D(width, height)

        val g = Grid2DSquare(
            height,
            width,
            depth,
            DistanceFunction.euclideanNorm2DTorus(
                intArrayOf(height, width),
                booleanArrayOf(false, true) // Wrap around width, but do not wrap around height.
            ),

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
        val exhibits = mutableListOf<Exhibit>()

        // Pick top image for every node and add it.
        // TODO Handle case for empty lists (can this even happen if we have more (distinct) samples than nodes?).
        for (idDistanceList in nodeMap.values) { // Linked hash map, ordered according to node ID.
            val c = idDistanceList[0].first

            val e = Exhibit(name = c, path = c + RequestContentHandler.URL_ID_SUFFIX)

            val imageBytes = cineastHttp.objectRequest(c)

            GenerationUtils.calculateExhibitSize(imageBytes, e, 2f)

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

                    exhibit.position = GenerationUtils.getWallPositionByCoords(i, j)

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
        // TODO Decide on some way to set a (non-random) exhibition name.
        val ex = Exhibition(name = "Generated Exhibition ${Random.nextInt()}")

        ex.rooms.addAll(rooms)

        return ex
    }

    fun genSomEx(): Exhibition {
        val allFeatures =
            CineastRest.getFeatureDataFromCategory(genConfig.genType.cineastCategory) // Get all features for category.

        // Pick the feature type we actually want.
        val features = allFeatures[genConfig.genType.tableName] ?: return Exhibition(name = "Empty Exhibition")

        // Remove IDs if we're filtering by ID list.
        // TODO If this is too expensive, create a new Cineast API call to obtain features for certain IDs only.
        features.removeNonListedIds(genConfig.idList)

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

        return createExhibitionFromRooms(arrayListOf(rooms))
    }

}
