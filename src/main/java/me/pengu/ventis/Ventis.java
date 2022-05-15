package me.pengu.ventis;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;
import me.pengu.ventis.messenger.Messenger;
import me.pengu.ventis.messenger.impl.redis.RedisMessenger;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Ventis, a simple and clean packet api.
 * Currently, supporting redis messaging.
 */
@Getter @Setter
public class Ventis {

    private VentisConfig config;
    private Messenger messenger;

    private final ExecutorService executor;
    private final Map<String, Entry<Class<? extends Packet>, List<PacketListenerData>>> packetListeners;

    /**
     * Ventis instance.
     * @param config selected config options {@link VentisConfig}
     */
    public Ventis(VentisConfig config) {
        this(config, config.getMessengerType());
    }

    /**
     * Ventis instance.
     * @param config selected config options {@link VentisConfig}
     * @param messengerType messenger type to initialize
     */
    public Ventis(VentisConfig config, String messengerType) {
        this.config = config;
        this.messenger = this.getMessagingFor(messengerType);

        this.executor = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("Ventis - Packet Thread - %d").build()
        );
        this.packetListeners = new ConcurrentHashMap<>();
    }

    public Messenger getMessagingFor(String messengerType) {
        if (messengerType == null || messengerType.isEmpty()) return null;

        switch (messengerType.toLowerCase()) {
            case "redis":
                return new RedisMessenger(this);
            case "sql":
                throw new UnsupportedOperationException(
                        "Sql messaging is currently unsupported."
                );
        }

        throw new IllegalArgumentException("Invalid provided messenger " + messengerType);
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
            Entry<Class<? extends Packet>, List<PacketListenerData>> packetListEntry = this.getPacketListeners().computeIfAbsent(
                    packetClass.getName(),
                    entry -> new SimpleEntry<>(redisPacket, new ArrayList<>())
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