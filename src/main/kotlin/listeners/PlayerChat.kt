package twizzy.tech.listeners

import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerChatEvent
import twizzy.tech.clerk.Profile

class PlayerChat {

    init {
        val Events = MinecraftServer.getGlobalEventHandler()
        Events.addListener(PlayerChatEvent::class.java) { event ->
            val player = event.player
            val rank = Profile.getProfile(player.uuid)?.getRank()?.prefix

            val message = "$rank ${player.username}§7: §f${event.rawMessage}"

            event.formattedMessage = Component.text(message)
        }
    }
}