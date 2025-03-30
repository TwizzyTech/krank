package twizzy.tech.commands

import kotlinx.coroutines.runBlocking
import revxrsal.commands.exception.CommandErrorException
import revxrsal.commands.minestom.actor.MinestomCommandActor
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.process.CommandCondition
import twizzy.tech.clerk.ClerkPermission
import twizzy.tech.clerk.Profile

object Permission : CommandCondition<MinestomCommandActor> {

    override fun test(context: ExecutionContext<MinestomCommandActor>) {
        // Check if the command has the @ClerkPermission annotation
        val clerkPermissionAnn = context.command().annotations().get(ClerkPermission::class.java)

        if (clerkPermissionAnn != null) {
            // Get the value of the @ClerkPermission annotation
            val actor = context.actor()
            val permission = clerkPermissionAnn.value
            runBlocking {
                val profile = Profile.getProfile(actor.uniqueId())
                if (profile == null || !profile.hasPermission(permission)) {
                    throw CommandErrorException("You do not have access to this command.")
                }
            }
        }
    }
}