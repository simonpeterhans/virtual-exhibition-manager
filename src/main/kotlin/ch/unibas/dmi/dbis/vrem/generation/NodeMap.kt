package ch.unibas.dmi.dbis.vrem.generation

import ch.unibas.dmi.dbis.som.PredictionResult

// TODO Replace MutableList<Pair<String, Double>> with IdDistanceList.
class NodeMap : LinkedHashMap<Int, MutableList<Pair<String, Double>>>() {

    fun addEmptyNode(i: Int) {
        this[i] = ArrayList()
    }

    fun addClassifiedSample(sampleId: String, res: PredictionResult) {
        if (this[res.nodeId] == null) {
            addEmptyNode(res.nodeId)
        }

        this[res.nodeId]!!.add(Pair(sampleId, res.distance))
    }

}
