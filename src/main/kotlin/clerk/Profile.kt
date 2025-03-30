package twizzy.tech.clerk

import com.google.gson.Gson
import kotlinx.coroutines.*
import net.minestom.server.entity.Player
import twizzy.tech.util.LettuceConnection
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

class Profile private constructor(
    val uuid: UUID,
    val username: String,
    val ipAddress: MutableList<String> = mutableListOf(),
    val settings: Settings = Settings(),
    val friends: MutableList<UUID> = mutableListOf(),
    val requests: MutableList<UUID> = mutableListOf(),
    val sentRequests: MutableList<UUID> = mutableListOf(),
    val ignoreList: MutableList<UUID> = mutableListOf()
) {
    val permissions: MutableSet<String> = mutableSetOf()
    val ranks: MutableList<String> = mutableListOf()

    data class Settings(
        var togglepm: Boolean = false,
        var friendRequests: Boolean = true
    )

    companion object {
        val profiles = ConcurrentHashMap<UUID, Profile>()
        private val gson = Gson()

        suspend fun retrieve(player: Player) {
            retrieve(player.uuid.toString(), player.username)
        }

        suspend fun retrieve(playerUuid: String, playerName: String? = null): Profile {
            val uuid = UUID.fromString(playerUuid)
            val cacheKey = "profiles:$playerUuid"

            return suspendCancellableCoroutine { continuation ->
                CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        val cachedProfile = LettuceConnection.get(cacheKey)
                        if (cachedProfile != null) {
                            val profileData = gson.fromJson(cachedProfile, ProfileData::class.java)
                            val profile = Profile(
                                uuid,
                                profileData.username,
                                profileData.ipAddress.toMutableList(),
                                profileData.settings,
                                profileData.friends.toMutableList(),
                                profileData.requests.toMutableList(),
                                profileData.sentRequests.toMutableList(),
                                profileData.ignoreList.toMutableList()
                            ).apply {
                                permissions.addAll(profileData.permissions)
                                ranks.addAll(profileData.ranks)
                            }

                            profiles[uuid] = profile // Store in memory
                            continuation.resume(profile)
                            break
                        } else {
                            delay(1000) // Wait for 1 second before checking again
                        }
                    }
                }
            }
        }

        fun getProfile(uuid: UUID): Profile? {
            return profiles[uuid]
        }
    }

    suspend fun hasPermission(permission: String): Boolean {
        if (permissions.contains(permission)) {
            return true
        }
        val parts = permission.split(".")
        for (i in parts.indices) {
            val wildcardPermission = parts.subList(0, i).joinToString(".") + ".*"
            if (permissions.contains(wildcardPermission)) {
                return true
            }
        }
        for (rankName in ranks) {
            val rank = RankManager.ranks.find { it.name == rankName }
            if (rank != null) {
                if (rank.permissions.contains(permission)) {
                    return true
                }
                for (i in parts.indices) {
                    val wildcardPermission = parts.subList(0, i).joinToString(".") + ".*"
                    if (rank.permissions.contains(wildcardPermission)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun getRank(): Rank? {
        return ranks.mapNotNull { rankName ->
            RankManager.ranks.find { it.name == rankName }
        }.maxByOrNull { it.weight }
    }

    data class ProfileData(
        val username: String,
        val permissions: List<String>,
        val ipAddress: List<String>,
        val ranks: List<String>,
        val settings: Settings = Settings(),
        val friends: List<UUID>,
        val requests: List<UUID>,
        val sentRequests: List<UUID>,
        val ignoreList: List<UUID>
    ) {
        constructor(profile: Profile) : this(
            profile.username,
            profile.permissions.toList(),
            profile.ipAddress,
            profile.ranks.distinct(), // Ensure ranks are unique
            profile.settings,
            profile.friends,
            profile.requests,
            profile.sentRequests,
            profile.ignoreList
        )
    }
}