package me.pengu.ventis;

import lombok.Getter;
import me.pengu.ventis.context.VentisContext;
import me.pengu.ventis.packet.Packet;
import me.pengu.ventis.packet.listener.PacketListenerData;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

/**
 * Ventis Subscriber
 * Extends {@link JedisPubSub} for message handling.
 */
public class VentisSubscriber extends JedisPubSub {

    @Getter public boolean closed;

    private final Ventis ventis;
    private final Jedis jedis;

    /**
     * Ventis Subscriber instance.
     * Initializes this as-well as subscribing to {@link Jedis}
     * @param ventis {@link Ventis} instance
     */
    public VentisSubscriber(Ventis ventis) {
        this.ventis = ventis;
        this.jedis = ventis.getJedisPool().getResource();

        this.ventis.getExecutor().submit(() ->
                this.jedis.subscribe(this, Ventis.CHANNEL_PREFIX + "*"));
    }

    /**
     * An implementation of {@link JedisPubSub#onMessage(String, String)}.
     * De-Serializes {@param message} data using provided {@link VentisContext}'s deserializer
     * @param channel channel to listen for
     * @param message provided data in form of a String
     */
    @Override
    public void onMessage(String channel, String message) {
        int messageIndex = message.indexOf(Ventis.SPLIT_REGEX);
        // using indexOf as it has better performance than split
        String packetName = message.substring(0, messageIndex);

        Entry<Class<? extends Packet>, List<PacketListenerData>> packetListEntry =
                this.ventis.getPacketListeners().get(packetName);
        if (packetListEntry == null) return;

        String data = message.substring(messageIndex + Ventis.SPLIT_REGEX.length());
        Class<? extends Packet> clazz = packetListEntry.getKey();

        Packet packet = this.ventis.getConfig().getContext().deSerialize(data, clazz);
        if (!clazz.getName().equalsIgnoreCase(packet.getClassName())) return; // Invalid packet, end.

        for (PacketListenerData packetListener : packetListEntry.getValue()) {
            if (packetListener.getChannels().length > 0 && !Arrays.asList(packetListener.getChannels())
                    .contains(channel.substring(channel.indexOf(":") + ":".length()))) continue;
            // This is checking the annotation @PacketHandler's channels [if they exist and if they match this channel]

            try {
                packetListener.getMethod().invoke(packetListener.getInstance(), packet);
            } catch (Exception e) {
                throw new RuntimeException(
                        String.format("Failed to parse %1$s because it has a invalid packet signature (%2$s).",
                                packetListener.getClass(), e.getMessage())
                );
            }
        }
    }

    /**
     * Cleanup this instance.
     * @see Ventis#close()
     */
    public void close() {
        if (this.isClosed()) return;

        if (super.isSubscribed()) {
            super.unsubscribe();
        }

        this.closed = true;
    }
}
