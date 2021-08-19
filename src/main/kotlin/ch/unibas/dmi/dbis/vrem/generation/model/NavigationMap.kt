package ch.unibas.dmi.dbis.vrem.generation.model

import kotlinx.serialization.Serializable

/**
 * Navigation map metadata used to keep track of predecessor/successor rooms in the tree.
 *
 * @property root The ID of the room figuring as the root (initial) room.
 * @property predecessor Predecessor map, mapping from room ID to the room ID of the room's predecessor room.
 * @property successor Successor map, mapping from the room ID to the room ID of the room's successor room.
 */
@Serializable
data class NavigationMap(
    val root: String,
    val predecessor: MutableMap<String, String?> = mutableMapOf(),
    val successor: MutableMap<String, String?> = mutableMapOf()
)
