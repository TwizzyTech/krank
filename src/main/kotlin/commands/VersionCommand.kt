package twizzy.tech.commands

import revxrsal.commands.annotation.Command
import revxrsal.commands.minestom.actor.MinestomCommandActor
import twizzy.tech.clerk.ClerkPermission

class VersionCommand {

    @Command("version")
    @ClerkPermission("commands.version")
    fun version(actor: MinestomCommandActor) {
        actor.reply("This server is running on version 1.0.0")
    }
}