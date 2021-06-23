package ch.unibas.dmi.dbis.vrem.generate

import ch.unibas.dmi.dbis.vrem.cineast.client.apis.SegmentsApi
import ch.unibas.dmi.dbis.vrem.cineast.client.infrastructure.ApiClient
import ch.unibas.dmi.dbis.vrem.cineast.client.models.*
import ch.unibas.dmi.dbis.vrem.import.ImportUtils
import ch.unibas.dmi.dbis.vrem.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.rest.handlers.RequestContentHandler.Companion.URL_ID_SUFFIX
import com.github.kittinunf.fuel.httpGet
import io.javalin.http.Context
import java.io.File
import java.time.Duration
import java.util.*


/**
 * Draft of a draft of the similarity generator.
 * Works, but is extremely static and a complete mess as of now.
 *
 * @constructor
 */
class SimilarityGenerator {

    fun getCandidates(ctx: Context?): MutableList<StringDoublePair> {
        // TODO Obtain image (or at least its ID) from post request.
        val img = Base64.getEncoder().encodeToString(File("01.jpg").readBytes())

        val containers = mutableListOf<QueryComponent>()
        containers.add(
            QueryComponent(
                mutableListOf(
                    QueryTerm(
                        QueryTerm.Type.IMAGE,
                        "data:image/png;base64,$img",
                        mutableListOf("localcolor")
                    )
                )
            )
        )

        val queryConfig = null
        val components = null

        val query = SimilarityQuery(containers, queryConfig, components) // Omit messageType or the query will fail.

        val apiClient = SegmentsApi()
        val request = apiClient.findSegmentSimilar(query)
        val res: SimilarityQueryResult? = request.results?.find { it.category == "localcolor" }

        // List of the retrieved image IDs (hardcoded 16 for now).
        val content: List<StringDoublePair> = res?.content!!.subList(0, 16) // Contains key and value.
        val candidates = mutableListOf<StringDoublePair>()

        // This is super ugly but only used for testing anyway.
        for (i in 0..11) {
            val curr = content[i]
            candidates.add(StringDoublePair(curr.key?.substring(0, curr.key.lastIndexOf("_")), curr.value))
        }

        return candidates
    }


    fun generate(ctx: Context?) {
        val candidates = getCandidates(ctx)
        val exhibition = Exhibition(name = "Similar Collection")
        val exhibits = mutableListOf<Exhibit>()

        // Create exhibits from obtained IDs.
        for (c in candidates) {
            val e = Exhibit(name = c.key.toString(), path = c.key.toString() + URL_ID_SUFFIX)

            // Get image to compute width/height.
            val (_, _, result) = "http://localhost:4567/objects/${c.key.toString()}"
                .httpGet()
                .response()

            val imageBytes = result.get()
            ImportUtils.calculateExhibitSize(imageBytes, e, 2f)

            exhibits.add(e)
        }

        // Create walls.
        val walls = mutableListOf<Wall>()

        // Add 1 wall for every direction.
        enumValues<Direction>().forEach { e -> walls.add(Wall(e, "CONCRETE")) }

        // Place them on every wall.
        for (w in walls) {
            for (i in 1..3) {
                val exhibit = exhibits.removeFirst()

                exhibit.position = ImportUtils.calculateWallExhibitPosition(exhibit, w.exhibits)

                w.exhibits.add(exhibit)
            }
        }

        val room = Room("room01")
        room.walls.addAll(walls)
        exhibition.addRoom(room)

        ctx?.json(exhibition)
    }

}

fun main() {
    ApiClient.builder.readTimeout(Duration.ofMillis(60_000))

    SimilarityGenerator().generate(null)
}
