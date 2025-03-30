package twizzy.tech.slayer.movetek

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityPose
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import revxrsal.commands.annotation.Command

class Skydive {

    @Command("sky")
    fun sky(actor: Player) {
        actor.teleport(Pos(0.0, 150.0, 0.0))
        actor.sendMessage("Skydiving!")

    }

    init {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent::class.java) { event ->
            val player = event.player

            if (!player.isOnGround && player.previousPosition.y > event.newPosition.y) {
                if (player.velocity.y < -15.0 && hasAirBelow(player, 10)) {
                    player.entityMeta.isFlyingWithElytra = true
                }
            }
        }

    }

    /**
     * Checks if there are at least `amount` air blocks below the player.
     */
    private fun hasAirBelow(player: Player, amount: Int): Boolean {
        val instance = player.instance ?: return false
        val position = player.position

        for (i in 1..amount) {
            val block = instance.getBlock(position.add(0.0, -i.toDouble(), 0.0))
            if (!block.isAir) return false
        }
        return true
    }
}