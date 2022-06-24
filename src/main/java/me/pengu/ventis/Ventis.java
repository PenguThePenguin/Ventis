package me.pengu.ventis;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;
import me.pengu.ventis.connection.Connection;
import me.pengu.ventis.packet.Packet;
import me.pengu.ventis.packet.handler.PacketHandler;
import me.pengu.ventis.packet.listener.PacketListener;
import me.pengu.ventis.packet.listener.PacketListenerData;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Ventis, a simple and clean packet api.
 * Supporting: redis, sql, socket and rabbitMq.
 */
@Getter @Setter
public class Ventis {

    private VentisConfig config;
    private final Map<String, Connection> connections;

    private final ScheduledThreadPoolExecutor executor;
    private final Map<String, Entry<Class<? extends Packet>, List<PacketListenerData>>> packetListeners;

    /**
     * Ventis instance.
     *
     * @param config selected config options {@link VentisConfig}
     */
    public Ventis(VentisConfig config) {
        this.config = config;
        this.connections = new ConcurrentHashMap<>();

        this.executor = new ScheduledThreadPoolExecutor(1,
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Ventis - Packet Thread - %d").build()
        );
        this.packetListeners = new ConcurrentHashMap<>();
    }

    /**
     * Registers a connection.
     *
     * @param connection connection instance to register
     */
    public void registerConnection(Connection connection) {
        this.connections.put(connection.getName(), connection);
    }

    /**
     * Un-registers a connection.
     *
     * @param connection instance to unregister
     */
    public void unregisterConnection(Connection connection) {
        this.connections.remove(connection.getName());
    }

    /**
     * Gets a connection type from its name and class
     *
     * @param name     The name of the connection.
     * @param classOfT the class of the connection type.
     * @return The connection instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends Connection> T getConnection(String name, Class<T> classOfT) {
        return (T) this.getConnection(name).getClass().cast(classOfT);
    }

    /**
     * Gets a connection type from its name
     *
     * @param name The name of the connection.
     * @return The connection instance.
     */
    public Connection getConnection(String name) {
        return this.connections.get(name);
    }

    /**
     * Registers only a certain packet inside a listener
     *
     * @param packet specified to register
     * @param listener instance to register
     */
    public void registerPacket(Class<? extends Packet> packet, PacketListener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.getParameterTypes().length != 1
                    || !packet.equals(method.getParameterTypes()[0])) continue;

            this.registerPacket(packet, method, listener);
            break;
        }
    }

    /**
     * Registers a listener as well as its packets.
     *
     * @param listener instance to register
     */
    public void registerListener(PacketListener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(PacketHandler.class)) continue;

            Class<?> packetClass = method.getParameterTypes()[0];
            if (method.getParameterTypes().length != 1
                    || !Packet.class.isAssignableFrom(packetClass)) {

                throw new IllegalArgumentException(
                        String.format("Failed to register %1$s as it isn't a instance of a Packet (%2$s)",
                                packetClass.getName(), listener.getClass().getName())
                );
            }

            Class<? extends Packet> packet = packetClass.asSubclass(Packet.class);
            this.registerPacket(packet, method, listener);
        }
    }

    /**
     * Registers a packet based off its method and listener
     *
     * @param packet type to register
     * @param method instance to register
     * @param listener instance to register
     */
    private void registerPacket(Class<? extends Packet> packet, Method method, PacketListener listener) {
        String[] channels = method.getDeclaredAnnotation(PacketHandler.class).channels();

        // Create an inner entry of packet's class and an empty list if not present
        Entry<Class<? extends Packet>, List<PacketListenerData>> packetListEntry = this.getPacketListeners().computeIfAbsent(
                packet.getName(),
                entry -> new SimpleEntry<>(packet, new ArrayList<>())
        );
        packetListEntry.getValue().add(new PacketListenerData(listener, method, channels));
    }

    /**
     * Cleans up this ventis instance.
     */
    public void close() {
        this.executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                Logger.getGlobal().severe("Timed out waiting for ventis executor to terminate");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}