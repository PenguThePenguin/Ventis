package me.pengu.ventis.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.pengu.ventis.Ventis;
import me.pengu.ventis.VentisConfig;
import me.pengu.ventis.context.VentisContext;
import me.pengu.ventis.packet.Packet;
import me.pengu.ventis.packet.listener.PacketListenerData;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Connection
 * An abstract class to be extended per database type.
 */
@Getter
public abstract class Connection {

    public static final String SPLIT_REGEX = "||";
    public static final String CHANNEL_PREFIX = "ventis-packet:";

    public final Ventis ventis;
    public final String name;
    public final VentisConfig config;

    public boolean connected;

    public Connection(Ventis ventis, String name, VentisConfig ventisConfig) {
        this.ventis = ventis;
        this.name = name;
        this.config = this.ventis.getConfig();

        this.ventis.register(this);
    }

    /**
     * Sends a packet.
     * @param packet packet to send
     * @return a future to manipulate the result of the operation
     */
    public CompletableFuture<Void> sendPacket(Packet packet) {
        return this.sendPacket(packet, this.config.getChannel());
    }

    /**
     * Sends a packet.
     * @param packet packet to send
     * @param channel redis channel to use
     *
     * @return a future to manipulate the result of the operation
     */
    public abstract CompletableFuture<Void> sendPacket(Packet packet, String channel);

    /**
     * De-Serializes {@param message} data using provided {@link VentisContext}'s deserializer
     * @param channel channel to listen for
     * @param message provided data in form of a String
     *
     * @return a boolean to check if packet is valid
     */
    public boolean handleMessage(String channel, String message) {
        int messageIndex = message.indexOf(Connection.SPLIT_REGEX);
        // using indexOf as it has better performance than split
        String packetName = message.substring(0, messageIndex);

        Map.Entry<Class<? extends Packet>, List<PacketListenerData>> packetListEntry =
                this.getVentis().getPacketListeners().get(packetName);
        if (packetListEntry == null) return false;

        String data = message.substring(messageIndex + Connection.SPLIT_REGEX.length());
        Class<? extends Packet> clazz = packetListEntry.getKey();

        Packet packet = this.getConfig().getContext().deSerialize(data, clazz);
        if (!clazz.getName().equalsIgnoreCase(packet.getClassName())) return false; // Invalid packet, end.

        for (PacketListenerData packetListener : packetListEntry.getValue()) {
            if (packetListener.getChannels().length > 0 && !Arrays.asList(packetListener.getChannels())
                    .contains(channel.substring(channel.indexOf(":") + ":".length()))) continue;
            // This is checking the annotation @PacketHandler's channels [if they exist and if they match this channel]

            try {
                packetListener.getMethod().invoke(packetListener.getInstance(), packet);
                return true;
            } catch (Exception e) {
                throw new RuntimeException(
                        String.format("Failed to parse %1$s because it has a invalid packet signature (%2$s).",
                                packetListener.getClass(), e.getMessage())
                );
            }
        }
        return false;
    }

    /**
     * Cleans up this connection instance.
     */
    public void close() {
        if (!this.connected) return;

        this.ventis.close();
        this.connected = false;
    }
}

