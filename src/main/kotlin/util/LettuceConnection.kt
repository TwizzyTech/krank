package twizzy.tech.util

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.api.reactive.RedisReactiveCommands
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands

object LettuceConnection {
    private val client: RedisClient
    private val connection: StatefulRedisConnection<String, String>
    private val syncCommands: RedisCommands<String, String>
    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    val asyncCommands: RedisCoroutinesCommands<String, String>
    val reactiveCommands: RedisReactiveCommands<String, String>
    private val pubSubConnection: StatefulRedisPubSubConnection<String, String>
    private val pubSubAsyncCommands: RedisPubSubAsyncCommands<String, String>

    init {
        val uri = RedisURI.Builder
            .redis("redis-16900.c281.us-east-1-2.ec2.redns.redis-cloud.com", 16900)
            .withPassword("1bTRP8jZe9Bb46XtlqVi6ADpEeasudS8".toCharArray())
            .withAuthentication("default", "1bTRP8jZe9Bb46XtlqVi6ADpEeasudS8".toCharArray())
            .build()

        client = RedisClient.create(uri)
        connection = client.connect()
        syncCommands = connection.sync()
        @OptIn(ExperimentalLettuceCoroutinesApi::class)
        asyncCommands = connection.coroutines()
        reactiveCommands = connection.reactive()

        pubSubConnection = client.connectPubSub()
        pubSubAsyncCommands = pubSubConnection.async()
    }

    // --- Key/Value Operations ---

    fun ping(): String = syncCommands.ping()

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun get(key: String): String? = asyncCommands.get(key)

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun setex(key: String, value: String, ttl: Long) {
        asyncCommands.setex(key, ttl, value)
    }

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun delete(key: String) {
        asyncCommands.del(key)
    }

    // --- Pub/Sub Operations ---

    fun subscribe(channel: String, listener: (String, String) -> Unit) {
        pubSubConnection.addListener(object : io.lettuce.core.pubsub.RedisPubSubListener<String, String> {
            override fun message(channel: String, message: String) {
                listener(channel, message)
            }

            override fun message(pattern: String, channel: String, message: String) {}
            override fun subscribed(channel: String, count: Long) {}
            override fun psubscribed(pattern: String, count: Long) {}
            override fun unsubscribed(channel: String, count: Long) {}
            override fun punsubscribed(pattern: String, count: Long) {}
        })
        pubSubAsyncCommands.subscribe(channel)
    }

    suspend fun publish(channel: String, message: String) {
        pubSubAsyncCommands.publish(channel, message)
    }

    // Shutdown all connections and the client.
    fun shutdown() {
        client.shutdown()
    }
}
