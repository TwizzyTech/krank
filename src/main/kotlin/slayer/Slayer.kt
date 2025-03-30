package twizzy.tech.slayer

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.EntityPose
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.event.player.PlayerStartSneakingEvent
import net.minestom.server.event.player.PlayerStopSneakingEvent
import net.minestom.server.utils.time.TimeUnit
import twizzy.tech.slayer.movetek.Dive
import twizzy.tech.slayer.movetek.Prone
import twizzy.tech.slayer.movetek.Skydive
import java.util.*

class Slayer {

    val bedrockPlayerList = mutableListOf<Player>()

    init {
        Prone()
        Dive()
        Skydive()

        MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent::class.java) { event ->
            val player = event.player

            if (player.isOnGround && event.newPosition.y > player.previousPosition.y) {
                player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0.01)
                player.exp = 0.0f // Set experience to 0 when the player first jumps
                jumpExhaustion(player)
                if (player.isSprinting) {
                    if (player.position.pitch > -10) {
                        player.isAllowFlying = true
                    }
                }
            }
        }

        // Combined Sprint and Jump Task
        MinecraftServer.getSchedulerManager().buildTask {
            val onlinePlayers = MinecraftServer.getConnectionManager().onlinePlayers
            for (player in onlinePlayers) {
                if (player.gameMode == GameMode.CREATIVE) {
                    continue
                }

                // Sprinting food exhaustion
                if (player.isSprinting) {
                    if (player.food > 0) {
                        player.food = (player.food - 1).coerceIn(0, 20)
                    }
                } else {
                    if (player.food < 20 && player.pose == EntityPose.STANDING) {
                        player.food = (player.food + 1).coerceIn(0, 20)
                    }
                }

                // Jump Strength Increase
                val jumpStrength = player.getAttributeValue(Attribute.JUMP_STRENGTH)
                if (jumpStrength < 0.6 && player.isOnGround && !player.isSneaking) {
                    val newJumpStrength = jumpStrength + 0.01
                    player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(newJumpStrength)
                    player.exp = (newJumpStrength / 0.6).coerceIn(0.0, 1.0).toFloat()
                } else if (jumpStrength >= 0.6) {
                    player.exp = 1.0f
                }
            }
        }.repeat(20, TimeUnit.SERVER_TICK).schedule()

        MinecraftServer.getGlobalEventHandler().addListener(PlayerStartSneakingEvent::class.java) { event ->
            val player = event.player
            player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0.0)
        }

        MinecraftServer.getGlobalEventHandler().addListener(PlayerStopSneakingEvent::class.java) { event ->
            val player = event.player
            val experience = player.exp.toDouble()
            val jumpStrength = (experience * 0.6).coerceIn(0.0, 0.6)
            player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(jumpStrength)
        }
    }

    fun setupSlayer(player: Player) {
        player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.14)
        player.getAttribute(Attribute.SCALE).setBaseValue(1.0)
        if (player.username.contains(".")) {
            bedrockPlayerList.add(player)
        }
    }

    fun jumpExhaustion(player: Player) {
        var task: net.minestom.server.timer.Task? = null
        task = MinecraftServer.getSchedulerManager().buildTask {
            val jumpStrength = player.getAttributeValue(Attribute.JUMP_STRENGTH)
            if (jumpStrength < 0.6 && player.isOnGround) {
                val newJumpStrength = jumpStrength + 0.01 // Use smaller increments for smoother increase
                player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(newJumpStrength)
                player.exp = (newJumpStrength / 0.6).coerceIn(0.0, 1.0).toFloat() // Ensure exp is between 0 and 1
            } else if (jumpStrength >= 0.6) {
                player.exp = 1.0f // Set experience to full when jump strength is at 0.6
                task?.cancel()
            }
        }.repeat(100, TimeUnit.MILLISECOND).schedule() // Use a shorter interval for smoother updates
    }
}