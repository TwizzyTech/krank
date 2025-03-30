package twizzy.tech.slayer.movetek

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerStartFlyingEvent
import java.util.concurrent.ConcurrentHashMap

class Dive {

    private val sneakTracker = ConcurrentHashMap<String, Long>()

    init {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerStartFlyingEvent::class.java) { event ->
            val player = event.player
            player.sendMessage("${player.velocity}")
            player.isAllowFlying = false
            player.isFlying = false
            initiateDive(player)
        }
    }

    private fun initiateDive(player: Player) {
        player.entityMeta.isFlyingWithElytra = true
        val direction = player.position.direction()
        player.velocity = Vec(direction.x() * 20.0, 0.0, direction.z() * 20)


    }
}