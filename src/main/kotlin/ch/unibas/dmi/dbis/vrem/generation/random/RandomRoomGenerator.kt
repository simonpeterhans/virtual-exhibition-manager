package ch.unibas.dmi.dbis.vrem.generation.random

import ch.unibas.dmi.dbis.vrem.generation.CineastClient
import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.RoomGenerator
import ch.unibas.dmi.dbis.vrem.model.exhibition.MetadataType
import ch.unibas.dmi.dbis.vrem.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.rest.requests.RandomGenerationRequest
import kotlin.math.min
import kotlin.random.Random

class RandomRoomGenerator(
    private val genConfig: RandomGenerationRequest,
    cineastHttp: CineastHttp
) : RoomGenerator(cineastHttp) {

    override val roomText = "Generated Room (Random)"

    override fun genRoom(): Room {
        var ids = if (genConfig.idList.isEmpty()) {
            CineastClient.getAllIds()
        } else {
            genConfig.idList
        }.shuffled(Random(genConfig.seed))

        ids = ids.subList(0, min(genConfig.roomSpec.height * genConfig.roomSpec.width, ids.size))

        val exhibits = idListToExhibits(ids)

        val walls = exhibitListToWalls(intArrayOf(genConfig.roomSpec.height, genConfig.roomSpec.width), exhibits)

        val room = wallsToRoom(walls)
        room.metadata[MetadataType.SEED.key] = genConfig.seed.toString()

        return room
    }

}
