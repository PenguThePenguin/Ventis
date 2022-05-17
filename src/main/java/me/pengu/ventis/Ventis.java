package me.pengu.ventis;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;
import me.pengu.ventis.connection.Connection;
import me.pengu.ventis.connection.implementation.redis.RedisConnection;
import me.pengu.ventis.connection.implementation.socket.SocketConnection;
import me.pengu.ventis.connection.implementation.sql.SqlConnection;
import me.pengu.ventis.packet.Packet;
import me.pengu.ventis.packet.handler.PacketHandler;
import me.pengu.ventis.packet.listener.PacketListener;
import me.pengu.ventis.packet.listener.PacketListenerData;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Ventis, a simple and clean packet api.
 * Currently, supporting redis messaging.
 */
@Getter @Setter
public class Ventis {

    private VentisConfig config;
    private final Map<String, Connection> connections;

    private final ScheduledThreadPoolExecutor executor;
    private final Map<String, Entry<Class<? extends Packet>, List<PacketListenerData>>> packetListeners;

    /**
     * Ventis instance.
     * @param config selected config options {@link VentisConfig}
     */
    public Ventis(VentisConfig config) {
        this(config, config.getConnectionType());
    }

    /**
     * Ventis instance.
     * @param config selected config options {@link VentisConfig}
     * @param connectionType connection type to initialize
     */
    public Ventis(VentisConfig config, String connectionType) {
        this.config = config;

        this.connections = new HashMap<>();
        if (connectionType != null && !connectionType.isEmpty()) {
            this.register(this.getConnectionFor(connectionType));
        }

        this.executor = new ScheduledThreadPoolExecutor(1,
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Ventis - Packet Thread - %d").build()
        );
        this.packetListeners = new ConcurrentHashMap<>();
    }

    public Connection getConnectionFor(String connectionType) {
        switch (connectionType.toLowerCase()) {
            case "redis":
                return new RedisConnection(this);
            case "sql":
                return new SqlConnection(this);
            case "socket":
                return new SocketConnection(this);
        }

        throw new IllegalArgumentException("Invalid provided connection " + connectionType);
    }

    public void register(Connection connection) {
        this.connections.put(connection.getName(), connection);
    }

    /**
     * Registers a listener as well as its packets.
     * @param packetListener listener instance to register
     */
    public void registerListener(PacketListener packetListener) {
        for (Method method : packetListener.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(PacketHandler.class)
                    || method.getParameters().length == 0) continue;

            Class<?> packetClass = method.getParameters()[0].getType();
            if (!Packet.class.isAssignableFrom(packetClass)) {
                throw new IllegalArgumentException(
                        String.format("Failed to register %1$s as it isn't a instance of a Packet (%2$s)",
                                packetClass.getName(), packetListener.getClass().getName())
                );
            }

            Class<? extends Packet> packet = packetClass.asSubclass(Packet.class);
            String[] channels = method.getDeclaredAnnotation(PacketHandler.class).channels();

            // Create an inner entry of packet's class and an empty list if not present
            Entry<Class<? extends Packet>, List<PacketListenerData>> packetListEntry = this.getPacketListeners().computeIfAbsent(
                    packetClass.getName(),
                    entry -> new SimpleEntry<>(packet, new ArrayList<>())
            );
            packetListEntry.getValue().add(new PacketListenerData(packetListener, method, channels));
        }
    }

    public void close() {
        this.executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES))
                Logger.getGlobal().severe("Timed out waiting for ventis executor to terminate");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}