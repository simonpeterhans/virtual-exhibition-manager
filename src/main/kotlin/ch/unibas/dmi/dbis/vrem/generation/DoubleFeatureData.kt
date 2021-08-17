package ch.unibas.dmi.dbis.vrem.generation

data class DoubleFeatureData(val featureName: String) {

    private val idMap: MutableMap<String, ArrayList<Double>> = mutableMapOf()

    fun addSample(id: String, value: ArrayList<Double>) {
        idMap[id] = value
    }

    fun getSortedIds(): ArrayList<String> {
        return idMap.keys.sorted().toCollection(ArrayList())
    }

    fun valuesTo2DArray(): Array<DoubleArray> {
        val arr = Array(idMap.size) { doubleArrayOf() }
        val keys = idMap.keys.sorted() // Make sure we have the keys sorted, this simplifies feature concatenation.
        var currIdx = 0

        for (e in keys) {
            arr[currIdx++] = idMap[e]!!.toDoubleArray()
        }

        return arr
    }

    fun removeNonListedIds(ids: ArrayList<String>) {
        ids.forEach(idMap::remove)
    }

}
