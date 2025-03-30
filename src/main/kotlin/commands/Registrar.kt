package twizzy.tech.commands

import revxrsal.commands.minestom.MinestomLamp
import twizzy.tech.commands.player.GamemodeCommand
import twizzy.tech.slayer.movetek.Prone
import twizzy.tech.slayer.movetek.Skydive

class Registrar {

    val lamp = MinestomLamp.builder()
        .commandCondition(Permission)
        .build()

    init {
        lamp.register(VersionCommand())
        lamp.register(GamemodeCommand())
        lamp.register(Prone())
        lamp.register(Skydive())
    }
}