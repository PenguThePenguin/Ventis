package me.pengu.ventis.connection.implementation.redis;

import lombok.Getter;
import me.pengu.ventis.Ventis;
import me.pengu.ventis.connection.Connection;
import me.pengu.ventis.connection.config.RedisConfig;
import me.pengu.ventis.packet.Packet;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Redis Connection
 * Extends {@link Connection} for packet handling.
 */
public class RedisConnection extends Connection {

    @Getter private final JedisPool jedisPool;

    private final RedisConfig redisConfig;
    private final RedisSubscriber subscriber;

    /**
     * Redis Connection instance.
     * @param ventis {@link Ventis} instance
     */
    public RedisConnection(Ventis ventis, RedisConfig redisConfig) {
        super(ventis, "redis");
        this.redisConfig = redisConfig;

        this.jedisPool = new JedisPool(
                new JedisPoolConfig(), this.redisConfig.getAddress(), this.redisConfig.getPort(),
                this.redisConfig.getTimeout(), this.redisConfig.isAuth() ? this.redisConfig.getPassword() : null
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

        try {
            return redisCommand.apply(jedis);
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            jedis = null; // To make sure that the jedis instance is not returned to the pool again.
            
            throw new JedisException(e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * Cleans up this redis instance.
     * @see Connection#close()
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