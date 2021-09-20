package ch.unibas.dmi.dbis.vrem.generation.similarity

import ch.unibas.dmi.dbis.vrem.cineast.client.apis.SegmentsApi
import ch.unibas.dmi.dbis.vrem.cineast.client.models.*
import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.RoomGenerator
import ch.unibas.dmi.dbis.vrem.model.exhibition.Direction
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.model.exhibition.Wall
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.rest.requests.SimilarityGenerationRequest
import kotlin.math.min

class SimilarityRoomGenerator(
    private val genConfig: SimilarityGenerationRequest,
    cineastHttp: CineastHttp
) : RoomGenerator(cineastHttp) {

    private val category = genConfig.genType.categoryName.first()

    override val roomText: String = "Generated Room (Similarity)"

    private fun similarExhibitsToWalls(
        dims: IntArray,
        queryExhibit: Exhibit,
        similarExhibits: MutableList<Exhibit>
    ): MutableList<Wall> {
        val walls = mutableListOf<Wall>()

        // Add a wall for every direction.
        enumValues<Direction>().forEach { e ->
            walls.add(Wall(e, "CONCRETE"))
        }

        // Place exhibits on the 3 remaining walls.
        for (i in 0 until dims[0]) {
            for (w in 1 until walls.size) { // Ignore north wall.
                for (j in 0 until dims[1] / walls.size) {
                    val exhibit = similarExhibits.removeFirst()

                    exhibit.position = getWallPositionByCoords(i, j)

                    walls[w].exhibits.add(exhibit)
                }
            }
        }

        // Set query exhibit position to center (take opposing wall as reference for width/height).
        val queryExhibitX = 0.5 * getRoomDimFromWalls(walls).x
        val queryExhibitY = getWallPositionByCoords(0, 0).y
        queryExhibit.position = Vector3f(queryExhibitX, queryExhibitY, 0.0f)

        // Add exhibit separately to north wall.
        walls[0].exhibits.add(queryExhibit)

        return walls
    }

    override fun genRoom(): Room {
        val containers = mutableListOf<QueryComponent>()
        containers.add(
            QueryComponent(
                mutableListOf(
                    QueryTerm(
                        QueryTerm.Type.ID,
                        genConfig.objectId,
                        mutableListOf(category)
                    )
                )
            )
        )

        val query = SimilarityQuery(containers) // Omit messageType or the query will fail.

        val apiClient = SegmentsApi()
        val request = apiClient.findSegmentSimilar(query)
        val res: SimilarityQueryResult? = request.results?.find { it.category == category }

        val numExhibits = (0.75 * (genConfig.roomSpec.height * genConfig.roomSpec.width) + 1).toInt()

        var content: List<StringDoublePair> = res?.content!! // Contains key and value.
        content = content.subList(0, min(numExhibits, content.size))

        val ids = content.map { it.key!! }.toList()

        val exhibits = idListToExhibits(ids)

        val walls = similarExhibitsToWalls(
            intArrayOf(genConfig.roomSpec.height, genConfig.roomSpec.width),
            exhibits[0], // First exhibit (queried).
            exhibits.subList(1, exhibits.size) // The other exhibits (results).
        )

        return wallsToRoom(walls)
    }

}
