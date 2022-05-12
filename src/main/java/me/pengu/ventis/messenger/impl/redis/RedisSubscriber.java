package me.pengu.ventis.messenger.impl.redis;

import lombok.Getter;
import me.pengu.ventis.messenger.Messenger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Redis Subscriber
 * Extends {@link JedisPubSub} for message handling.
 */
public class RedisSubscriber extends JedisPubSub {

    @Getter public boolean closed;

    private final RedisMessenger messenger;
    private final Jedis jedis;

    /**
     * Redis Subscriber instance.
     * Initializes this as-well as subscribing to {@link Jedis}
     *
     * @param messenger {@link RedisMessenger} instance
     */
    public RedisSubscriber(RedisMessenger messenger) {
        this.messenger = messenger;
        this.jedis = messenger.getJedisPool().getResource();

        this.messenger.getVentis().getExecutor().submit(() ->
                this.jedis.subscribe(this, Messenger.CHANNEL_PREFIX + "*")
        );
    }

    /**
     * An implementation of {@link JedisPubSub#onMessage(String, String)}.
     * @param channel channel to listen for
     * @param message provided data in form of a String
     */
    @Override
    public void onMessage(String channel, String message) {
        this.messenger.handleMessage(channel, message);
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
