package ch.unibas.dmi.dbis.vrem.generation.som

import ch.unibas.dmi.dbis.som.PredictionResult
import ch.unibas.dmi.dbis.som.SOM
import ch.unibas.dmi.dbis.som.grids.Grid2DSquare
import ch.unibas.dmi.dbis.som.util.DistanceFunction
import ch.unibas.dmi.dbis.som.util.DistanceScalingFunction
import ch.unibas.dmi.dbis.som.util.TimeFunction
import ch.unibas.dmi.dbis.vrem.generation.CineastClient
import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.RoomGenerator
import ch.unibas.dmi.dbis.vrem.generation.model.DoubleFeatureData
import ch.unibas.dmi.dbis.vrem.generation.model.IdDoublePair
import ch.unibas.dmi.dbis.vrem.generation.model.NodeMap
import ch.unibas.dmi.dbis.vrem.model.exhibition.MetadataType
import ch.unibas.dmi.dbis.vrem.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.model.exhibition.Wall
import ch.unibas.dmi.dbis.vrem.rest.requests.SomGenerationRequest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.lang.Double.max
import java.lang.Double.min
import kotlin.random.Random

class SomRoomGenerator(
    private val genConfig: SomGenerationRequest,
    cineastHttp: CineastHttp
) : RoomGenerator(cineastHttp) {

    override val roomText = "Generated Room (SOM)"

    private fun getFeatures(): DoubleFeatureData {
        val featureDataList = arrayListOf<DoubleFeatureData>()

        for (tablePair in genConfig.genType.featureList) {
            val featureData = CineastClient.getFeatureDataFromTableName(tablePair.id, genConfig.idList)

            // Normalize data.
            featureData.normalize(tablePair.value)

            featureDataList.add(featureData)
        }

        return DoubleFeatureData.concatenate(featureDataList)
    }

    private fun getFeatureRanges(features: Array<DoubleArray>): Pair<DoubleArray, DoubleArray> {
        val len = features[0].size
        val ranges = Pair(DoubleArray(len), DoubleArray(len))

        for (j in 0 until len) {
            var minVal = Double.MAX_VALUE
            var maxVal = Double.MIN_VALUE

            for (i in features.indices) {
                minVal = min(features[i][j], minVal)
                maxVal = max(features[i][j], minVal)
            }

            ranges.first[j] = minVal
            ranges.second[j] = (maxVal)
        }

        return ranges
    }

    private fun trainSom(features: Array<DoubleArray>): SOM {
        val height = genConfig.roomSpec.height
        val width = genConfig.roomSpec.width
        val epochs = 20_000 / features.size // TODO Find a smart way to determine this.
        val seed = Random(genConfig.seed)

        val g = Grid2DSquare(
            height,
            width,
            neighborhoodFunction = DistanceFunction.euclideanNorm2DTorus(
                intArrayOf(height, width),
                booleanArrayOf(false, true) // Wrap around width, but do not wrap around height.
            ),
            rand = seed
        )

        // Initialize weights based on the feature ranges we have.
        val ranges = getFeatureRanges(features)
        g.initializeWeights(features[0].size, ranges.first, ranges.second)

        val s = SOM(
            g,
            distanceScaling = DistanceScalingFunction.exponentialDecreasing(),
            alpha = TimeFunction.linearDecreasingFactorScaled(1.0),
            sigma = TimeFunction.defaultSigmaFunction(intArrayOf(height, width), 2.0),
            rand = seed
        )

        s.train(features, epochs)

        return s
    }

    private fun predictionsToNodeMap(
        numNodes: Int,
        predictions: ArrayList<PredictionResult>,
        ids: ArrayList<String>
    ): NodeMap {
        val nodeMap = NodeMap()

        // Add all nodes to the map.
        for (i in 0 until numNodes) {
            nodeMap.addEmptyNode(i)
        }

        // Add every classified sample to the corresponding node.
        for (i in ids.indices) {
            nodeMap.addClassifiedSample(ids[i], predictions[i])
        }

        // Sort lists.
        for ((k, v) in nodeMap.map.entries) {
            // Sort ascending which is what we want (smaller distance = more similar).
            nodeMap.map[k] = v.sortedWith(compareBy(IdDoublePair::value)).toCollection(ArrayList())
        }

        return nodeMap
    }

    private fun createWallsFromNodeMap(dims: IntArray, nodeMap: NodeMap): MutableList<Wall> {
        val idList = mutableListOf<String?>()

        // Pick top image for every node and add it.
        for (idDistanceList in nodeMap.map.values) { // Linked hash map, ordered according to node ID.
            if (idDistanceList.isEmpty()) {
                idList.add(null)
            } else {
                idList.add(idDistanceList[0].id)
            }
        }

        /*
         * The height dimension of the SOM grid is inverted here but that doesn't matter for the visualization
         * since we do not have any preset weights for specific SOM nodes when generating the SOM.
         */
        val exhibits = idListToExhibits(idList)

        for (i in exhibits.indices) {
            exhibits[i].metadata[MetadataType.MEMBER_IDS.key] =
                Json.encodeToString(ListSerializer(IdDoublePair.serializer()), nodeMap.map[i]!!)
        }

        return exhibitListToWalls(dims, exhibits)
    }

    override fun genRoom(): Room {
        val features = getFeatures()

        // Get all values as 2D array.
        val data = features.valuesTo2DArray() // Same order as the IDs.
        val ids = features.getSortedIds()

        // Train SOM.
        val som = trainSom(data)

        // Predict data.
        val predictions = som.predict(data)

        // Create node map.
        val nodeMap = predictionsToNodeMap(som.grid.getSize(), predictions, ids)

        // Create exhibitions depending on settings (we could create the sub-rooms right away as well).
        val walls = createWallsFromNodeMap(som.grid.dims, nodeMap)

        val room = wallsToRoom(walls)
        room.metadata[MetadataType.SEED.key] = genConfig.seed.toString()

        return room
    }

}
