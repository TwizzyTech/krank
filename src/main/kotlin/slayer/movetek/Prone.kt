package twizzy.tech.slayer.movetek

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.event.player.PlayerStartSneakingEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket
import net.minestom.server.network.packet.server.play.BlockChangePacket
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket
import net.minestom.server.utils.time.TimeUnit
import revxrsal.commands.annotation.Command
import twizzy.tech.slayer.Slayer
import java.util.concurrent.ConcurrentHashMap

class Prone {

    @Command("prone", "crawl")
    fun prone(actor: Player) {
        initiateProne(actor)
    }

    private val sneakTracker = ConcurrentHashMap<String, Long>()
    private val shulkerTracker = ConcurrentHashMap<Player, EntityCreature>()

    init {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerStartSneakingEvent::class.java) { event ->
            val player = event.player
            if (player.position.pitch >= 60.0) { // If the player is looking down
                val currentTime = System.currentTimeMillis()
                val playerId = player.uuid.toString()
                val lastSneakTime = sneakTracker[playerId]
                if (lastSneakTime != null && currentTime - lastSneakTime <= 500) { // 500ms sneak threshold
                    initiateProne(player)
                    sneakTracker.remove(playerId)
                } else {
                    sneakTracker[playerId] = currentTime
                }
            }
        }

        MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent::class.java) { event ->
            val player = event.player
            if (shulkerTracker.containsKey(player)) {
                updateShulkerPosition(player)
                if (player.position.y > player.previousPosition.y) {
                    removeShulker(player)
                }
            }
        }
    }

    fun initiateProne(player: Player) {
        val shulker = EntityCreature(EntityType.SHULKER)
        shulker.isInvulnerable = true
        shulker.isInvisible = true
        shulker.setNoGravity(true)
        shulker.setInstance(player.instance, player.position)
        shulker.teleport(player.position.add(0.0, 1.1, 0.0))
        shulkerTracker[player] = shulker
        player.isSneaking = true
    }

    // Track last barrier positions for each viewer
    private val lastBarrierPositions = mutableMapOf<Player, Pos>()

    private fun updateShulkerPosition(player: Player) {
        val shulker = shulkerTracker[player] ?: return
        val currentPos = player.position.add(0.0, 1.1, 0.0)
        shulker.teleport(currentPos)

        // Calculate barrier position based on current player position
        val barrierPos = player.position.add(0.0, 1.0, 0.0).withView(player.position)

        shulker.viewers.filter { it.username.startsWith(".") }.forEach { viewer ->
            // Remove previous barrier
            lastBarrierPositions[viewer]?.let { oldPos ->
                viewer.sendPackets(BlockChangePacket(oldPos, Block.AIR))
            }

            // Set new barrier
            viewer.sendPackets(
                BlockChangePacket(barrierPos, Block.BARRIER),
                // Optional: Add block break animation for smoother transition
                BlockBreakAnimationPacket(0, barrierPos, 0)
            )

            // Update last known position
            lastBarrierPositions[viewer] = barrierPos
        }
    }

    // Cleanup when players leave
    fun handleDisconnect(player: Player) {
        lastBarrierPositions.remove(player)
    }

    fun removeShulker(player: Player) {
        val shulker = shulkerTracker.remove(player) ?: return
        shulker.remove()
        player.isSneaking = false
    }
}