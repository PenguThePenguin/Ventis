package me.pengu.ventis;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import me.pengu.ventis.command.VentisCommand;
import me.pengu.ventis.packet.Packet;
import me.pengu.ventis.packet.handler.PacketHandler;
import me.pengu.ventis.packet.listener.PacketListener;
import me.pengu.ventis.packet.listener.PacketListenerData;
import redis.clients.jedis.*;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Ventis, a simple and clean api built off Redis (https://redis.io/)
 * using [{@link JedisPool} and {@link JedisPubSub}]
 */
@Getter
public class Ventis {

    public static final String SPLIT_REGEX = "||";
    public static final String CHANNEL_PREFIX = "ventis-packet:";

    private boolean connected;
    private final VentisConfig config;

    private final ExecutorService executor;
    // packet name -> (packet's class + list of its data).
    private final Map<String, Entry<Class<? extends Packet>, List<PacketListenerData>>> packetListeners;

    private final JedisPool jedisPool;
    private final VentisSubscriber subscriber;

    /**
     * Ventis instance.
     * @param config selected config options {@link VentisConfig}
     */
    public Ventis(VentisConfig config) {
        this.config = config;

        this.executor = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("Ventis - Redis Packet Thread - %d").build()
        );
        this.packetListeners = new ConcurrentHashMap<>();

        this.jedisPool = new JedisPool(
                new JedisPoolConfig(), config.getAddress(), config.getPort(),
                Protocol.DEFAULT_TIMEOUT, config.isAuth() ? config.getPassword() : null
        );

        this.subscriber = new VentisSubscriber(this);
        this.connected = true;
    }

    /**
     * Registers a listener as well as its packets.
     * @param packetListener listener instance to register
     */
    public void registerListener(PacketListener packetListener) {
        for (Method method : packetListener.getClass().getDeclaredMethods()) {
            if (method.getDeclaredAnnotation(PacketHandler.class) == null
                    || method.getParameters().length == 0) continue;

            Class<?> packetClass = method.getParameters()[0].getType();
            if (!Packet.class.isAssignableFrom(packetClass)) {
                throw new IllegalArgumentException(
                        String.format("Failed to register %1$s as it isn't a instance of a Packet (%2$s)",
                                packetClass.getName(), packetListener.getClass().getName())
                );
            }

            Class<? extends Packet> redisPacket = packetClass.asSubclass(Packet.class);
            String[] channels = method.getDeclaredAnnotation(PacketHandler.class).channels();

            // Create an inner entry of redisPacket's class and an empty list if not present
            Entry<Class<? extends Packet>, List<PacketListenerData>> packetListEntry = this.packetListeners.computeIfAbsent(
                    packetClass.getName(),
                    entry -> new SimpleEntry<>(redisPacket, new ArrayList<>())
            );
            packetListEntry.getValue().add(new PacketListenerData(packetListener, method, channels));
        }
    }

    /**
     * Sends a packet.
     * @param packet packet to send
     */
    public void sendPacket(Packet packet) {
        this.sendPacket(packet, this.config.getChannel());
    }

    /**
     * Sends a packet.
     * @param packet packet to send
     * @param channel redis channel to use
     */
    public void sendPacket(Packet packet, String channel) {
        this.executor.submit(() -> this.runCommand(redis ->
                redis.publish(CHANNEL_PREFIX + channel, packet.toString(this.config.getContext()))
        ));
    }

    /**
     * Runs a command on this jedisPool.
     * See {@link VentisCommand} for a detailed explanation
     * @param ventisCommand<T> the command to execute
     * @return the Generic <T> value
     */
    public <T> T runCommand(VentisCommand<T> ventisCommand) {
        if (!this.isConnected()) return null;

        Jedis jedis = this.jedisPool.getResource();
        T result = null;

        try {
            result = ventisCommand.execute(jedis);
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
     * Cleans up this Ventis instance.
     */
    public void close() {
        if (!this.isConnected()) return;

        this.runCommand(Jedis::save);

        if (!this.subscriber.isClosed()) {
            this.subscriber.close();
        }

        this.executor.shutdownNow();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES))
                Logger.getGlobal().severe("Timed out waiting for redis executor to terminate");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!this.jedisPool.isClosed()) {
            this.jedisPool.close();
        }

        this.packetListeners.clear();
        this.connected = false;
    }
}