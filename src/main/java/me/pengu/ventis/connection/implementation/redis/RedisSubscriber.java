package me.pengu.ventis.connection.implementation.redis;

import lombok.Getter;
import me.pengu.ventis.connection.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Redis Subscriber
 * Extends {@link JedisPubSub} for message handling.
 */
public class RedisSubscriber extends JedisPubSub {

    @Getter private boolean closed;
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
     */
    public void close() {
        if (this.isClosed()) return;

        if (super.isSubscribed()) {
            super.unsubscribe();
        }

        this.closed = true;
    }
}