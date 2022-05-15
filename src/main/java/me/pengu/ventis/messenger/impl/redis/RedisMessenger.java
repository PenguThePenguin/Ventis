package me.pengu.ventis.messenger.impl.redis;

import lombok.Getter;
import me.pengu.ventis.Ventis;
import me.pengu.ventis.messenger.Messenger;
import me.pengu.ventis.messenger.config.RedisConfig;
import me.pengu.ventis.packet.Packet;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Redis Messenger
 * Extends {@link Messenger} for packet handling.
 */
public class RedisMessenger extends Messenger {

    @Getter private final JedisPool jedisPool;
    private final RedisSubscriber subscriber;

    /**
     * Redis Messenger instance.
     * @param ventis {@link Ventis} instance
     */
    public RedisMessenger(Ventis ventis) {
        super(ventis);

        RedisConfig redisConfig = this.config.getRedisConfig();

        this.jedisPool = new JedisPool(
                new JedisPoolConfig(), redisConfig.getAddress(), redisConfig.getPort(),
                Protocol.DEFAULT_TIMEOUT, redisConfig.isAuth() ? redisConfig.getPassword() : null
        );
        this.subscriber = new RedisSubscriber(this);
    }

    /**
     * Sends a packet.
     * @param packet packet to send
     * @param channel redis channel to use
     *
     @return a future to manipulate the result of the operation
     */
    @Override
    public CompletableFuture<Void> sendPacket(Packet packet, String channel) {
        return CompletableFuture.runAsync(() ->
                this.runCommand(redis ->
                        redis.publish(CHANNEL_PREFIX + channel, packet.toString(this.config.getContext()))
                ), this.ventis.getExecutor()
        );
    }

    /**
     * Runs a command on this jedisPool.
     * @param redisCommand<T> the command to execute
     * @return the Generic <T> value
     */
    public <T> T runCommand(Function<Jedis, T> redisCommand) {
        if (!this.isConnected()) return null;

        Jedis jedis = this.jedisPool.getResource();
        T result = null;

        try {
            result = redisCommand.apply(jedis);
        } catch (Exception e) {
            e.printStackTrace();

            if (jedis != null) {
                this.jedisPool.returnBrokenResource(jedis);
            }
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }

        return result;
    }

    /**
     * Cleans up this redis instance.
     * @see Messenger#close()
     */
    @Override
    public void close() {
        this.runCommand(Jedis::save);

        if (!this.subscriber.isClosed()) {
            this.subscriber.close();
        }

        if (!this.jedisPool.isClosed()) {
            this.jedisPool.close();
        }

        super.close();
    }
}
