package ch.unibas.dmi.dbis.vrem.generation.model

enum class SimGenType(
    val categoryName: ArrayList<String> = arrayListOf()
) {

    SEMANTIC(
        arrayListOf("semantic")
    ),

    VISUAL(
        arrayListOf("visual")
    )

}
