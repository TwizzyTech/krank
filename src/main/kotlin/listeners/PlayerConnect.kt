package twizzy.tech.listeners

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent
import twizzy.tech.clerk.Profile
import twizzy.tech.clerk.RankManager
import twizzy.tech.util.LettuceConnection
import java.util.*

class PlayerConnect(private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {

    init {
        val globalEventHandler = MinecraftServer.getGlobalEventHandler()
        globalEventHandler.addListener(AsyncPlayerPreLoginEvent::class.java) { event ->
            // Using runBlocking here to wait for the suspend function
            runBlocking {
                val gameProfile = event.gameProfile
                val profile = Profile.retrieve(gameProfile.uuid.toString(), gameProfile.name)
                println("[clerk] Successfully found '${profile.username}' in the Lettuce stream.")
            }
        }

        // Subscribe to Redis channel for profile updates
        LettuceConnection.subscribe("profile-updates") { channel, message ->
            scope.launch {
                try {
                    val profileUuid = UUID.fromString(message.removePrefix("profiles:"))
                    Profile.retrieve(profileUuid.toString())
                    println("[clerk] An update was received for '$profileUuid' from the Lettuce stream.")
                } catch (e: IllegalArgumentException) {
                    println("[clerk] Invalid UUID string received: $message")
                }
            }
        }

        // Subscribe to Redis channel for rank updates
        LettuceConnection.subscribe("rank-update") { channel, message ->
            scope.launch {
                try {
                    RankManager.initializeRanks()
                    println("[clerk] An update was received for '$message' rank from the Lettuce stream.")
                } catch (e: Exception) {
                    println("[clerk] Failed to update ranks: ${e.message}")
                }
            }
        }
    }
}