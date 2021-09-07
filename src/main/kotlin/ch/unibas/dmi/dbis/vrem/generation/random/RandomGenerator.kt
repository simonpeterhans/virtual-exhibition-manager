package ch.unibas.dmi.dbis.vrem.generation.random

import ch.unibas.dmi.dbis.vrem.generation.CineastClient
import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.Generator
import ch.unibas.dmi.dbis.vrem.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.model.exhibition.Wall
import ch.unibas.dmi.dbis.vrem.rest.requests.GenerationRequest
import kotlin.math.min
import kotlin.random.Random

class RandomGenerator(
    genConfig: GenerationRequest,
    cineastHttp: CineastHttp
) : Generator(genConfig, cineastHttp) {

    override val roomText = "Generated Room (Random)"

    override fun genRoom(): Room {
        var ids = if (genConfig.idList.isEmpty()) {
            CineastClient.getAllIds()
        } else {
            genConfig.idList
        }.shuffled(Random(genConfig.seed))

        ids = ids.subList(0, min(genConfig.height * genConfig.width, ids.size))

        val exhibits = idListToExhibits(ids)

        val walls: MutableList<Wall> = exhibitListToWalls(intArrayOf(genConfig.height, genConfig.width), exhibits)

        return wallsToRoom(walls)
    }

}
