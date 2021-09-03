package ch.unibas.dmi.dbis.vrem.model.exhibition

enum class MetadataType(val key: String = "") {

    GENERATED("generated"),
    PREDECESSOR("predecessor"),
    MEMBER_IDS("memberIds"),
    SEED("seed")

}
