package twizzy.tech.commands.player

import net.minestom.server.entity.GameMode
import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor
import twizzy.tech.clerk.ClerkPermission

class GamemodeCommand {

    @Command("gamemode", "gm")
   // @ClerkPermission("commands.gamemode")
    fun gamemode(
        actor: MinestomCommandActor,
        gamemode: GameMode
    ){
        actor.asPlayer()?.gameMode = gamemode
    }
}