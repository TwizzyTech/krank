package twizzy.tech.clerk

import org.bson.Document
import twizzy.tech.util.StreamConnection

data class Rank(
    val name: String,
    var weight: Int,
    var prefix: String,
    val permissions: MutableList<String>,
    val inherit: MutableList<String>
)

object RankManager {
    const val DEFAULT_RANK_NAME = "Recruit"

    // Define your default ranks.
    private val defaultRanks = listOf(
        Rank(
            name = DEFAULT_RANK_NAME,
            weight = 0,
            prefix = "[Recruit]",
            permissions = mutableListOf("commands.version"),
            inherit = mutableListOf()
        ),
    )

    // This list will hold all ranks loaded from the DB.
    val ranks = mutableListOf<Rank>()

    /**
     * Initializes the ranks by loading all rank documents from the MongoDB collection.
     * If no ranks are found in the DB, it falls back to inserting and using the default ranks.
     */
    suspend fun initializeRanks() {
        val docs: List<Document> = StreamConnection.readAllAsync("ranks")
        if (docs.isNotEmpty()) {
            // Clear any existing in-memory ranks and load from DB.
            ranks.clear()
            for (doc in docs) {
                val rank = Rank(
                    name = doc.getString("_id"),
                    weight = doc.getInteger("weight", 0),
                    prefix = doc.getString("prefix") ?: "",
                    permissions = (doc.getList("permissions", String::class.java) ?: emptyList()).toMutableList(),
                    inherit = (doc.getList("inherit", String::class.java) ?: emptyList()).toMutableList()
                )
                ranks.add(rank)
            }
            println("Loaded ${ranks.size} ranks from the database.")
        } else {
            // If the DB is empty, fall back to default ranks.
            ranks.addAll(defaultRanks)
            println("No ranks found in the database. Using default ranks.")
        }
    }

    private fun rankToDocument(rank: Rank): Document {
        return Document("_id", rank.name)
            .append("weight", rank.weight)
            .append("prefix", rank.prefix)
            .append("permissions", rank.permissions)
            .append("inherit", rank.inherit)
    }
}