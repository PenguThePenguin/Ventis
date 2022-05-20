package me.pengu.ventis.context;

import me.pengu.ventis.packet.Packet;

/**
 * Ventis Context
 * A interface for any [serializer / de-serializer] to use.
 */
public interface VentisContext {

    /**
     * Serializes the specified packet.
     *
     * @param packet the packet to serialize.
     * @return the serialized packet as a string.
     */
    String serialize(Packet packet);

    /**
     * De-Serializes a string
     *
     * @param data        the String to deserialize.
     * @param packetClass The Class of the packet.
     * @return the packet decoded from the String.
     */
    Packet deSerialize(String data, Class<? extends Packet> packetClass);

}