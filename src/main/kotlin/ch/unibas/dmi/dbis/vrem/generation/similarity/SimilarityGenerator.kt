package ch.unibas.dmi.dbis.vrem.generation.similarity

import ch.unibas.dmi.dbis.vrem.cineast.client.apis.SegmentsApi
import ch.unibas.dmi.dbis.vrem.cineast.client.models.*
import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import ch.unibas.dmi.dbis.vrem.generation.CineastClient
import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.Generator
import ch.unibas.dmi.dbis.vrem.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.model.exhibition.Wall
import ch.unibas.dmi.dbis.vrem.rest.requests.GenerationRequest
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.util.*
import kotlin.math.min

class SimilarityGenerator(
    val cineastConfig: CineastConfig,
    genConfig: GenerationRequest,
    cineastHttp: CineastHttp
) : Generator(genConfig, cineastHttp) {

    private val dataString = "data:image/png;base64,"

    override val roomText: String = "Generated Room (Similarity)"

    override fun genRoom(): Room {
        // First ID in list is the image to query for.
        val imageId = genConfig.idList[0]

        val (_, _, result) = cineastConfig.getCineastObjectUrlString(imageId).httpGet().response()

        val bytes: ByteArray = when (result) {
            is Result.Failure -> return Room(text = roomText + " " + getTextSuffix())
            is Result.Success -> result.get()
        }

        val img = Base64.getEncoder().encodeToString(bytes)

        val containers = mutableListOf<QueryComponent>()
        containers.add(
            QueryComponent(
                mutableListOf(
                    QueryTerm(
                        QueryTerm.Type.IMAGE,
                        "$dataString$img",
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

        var content: List<StringDoublePair> = res?.content!! // Contains key and value.

        content = content.subList(0, min(genConfig.height * genConfig.width, content.size))

        val ids = content.map { it.key!!.substringBeforeLast(CineastClient.SEGMENT_SUFFIX) }.toList()

        val exhibits = idListToExhibits(ids)

        val walls: MutableList<Wall> = exhibitListToWalls(intArrayOf(genConfig.height, genConfig.width), exhibits)

        return wallsToRoom(walls)
    }

}
