package twizzy.tech

import io.github._4drian3d.signedvelocity.minestom.SignedVelocity
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.chunk.ChunkSupplier
import twizzy.tech.clerk.RankManager
import twizzy.tech.commands.Registrar
import twizzy.tech.listeners.PlayerChat
import twizzy.tech.listeners.PlayerConnect
import twizzy.tech.slayer.Slayer

suspend fun main() {

    val server = MinecraftServer.init()
    SignedVelocity.initialize()


    // Initialization
    RankManager.initializeRanks()
    Registrar()

    // Create the instance
    val instanceManager = MinecraftServer.getInstanceManager()
    val instanceContainer = instanceManager.createInstanceContainer()

    // Set the ChunkGenerator
    instanceContainer.setGenerator { unit ->
        unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK)
    }


    // Set the chunk supplier with a custom implementation
    instanceContainer.setChunkSupplier(object : ChunkSupplier {
        override fun createChunk(instance: Instance, x: Int, z: Int): LightingChunk {
            return LightingChunk(instance, x, z) // Create LightingChunk with Instance, x, and z
        }
    })

    // Add an event callback to specify the spawning instance (and the spawn position)
    val globalEventHandler = MinecraftServer.getGlobalEventHandler()
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player
        event.spawningInstance = instanceContainer
        player.respawnPoint = Pos(0.0, 42.0, 0.0)
        player.gameMode = GameMode.SURVIVAL
        Slayer().setupSlayer(player)
    }

    // Enable Velocity Proxy
    VelocityProxy.enable("gHi7VvKJ3oXv")

    server.start("0.0.0.0", 25566)

    PlayerConnect()
    PlayerChat()
    Slayer()

}