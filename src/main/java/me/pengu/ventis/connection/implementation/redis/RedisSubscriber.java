package me.pengu.ventis.connection.implementation.redis;

import me.pengu.ventis.connection.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Redis Subscriber
 * Extends {@link JedisPubSub} for packet handling.
 */
public class RedisSubscriber extends JedisPubSub {

    private final RedisConnection connection;

    /**
     * Redis Subscriber instance.
     * Initializes this as-well as subscribing to {@link Jedis}
     *
     * @param connection {@link RedisConnection} instance
     */
    public RedisSubscriber(RedisConnection connection) {
        this.connection = connection;

        this.connection.getVentis().getExecutor().submit(() ->
                this.connection.runCommand(redis -> {
                    redis.subscribe(this, Connection.CHANNEL_PREFIX + "*");
                    return null;
                })
        );
    }

    /**
     * An implementation of {@link JedisPubSub#onMessage(String, String)}.
     *
     * @param channel channel to listen for
     * @param message provided data in form of a String
     */
    @Override
    public void onMessage(String channel, String message) {
        this.connection.handleMessage(channel, message);
    }

    /**
     * Cleans up this subscriber instance.
     *
     * @see RedisConnection#close()
     */
    public void close() {
        if (super.isSubscribed()) super.unsubscribe();
    }
}