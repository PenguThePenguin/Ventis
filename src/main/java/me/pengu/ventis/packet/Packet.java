package me.pengu.ventis.packet;

import lombok.Data;
import me.pengu.ventis.codec.VentisCodec;
import me.pengu.ventis.connection.Connection;

/**
 * Represents data that can be sent.
 * @see Connection#sendPacket(Packet)
 */
@Data
public class Packet {

    /**
     * Gets this packet as a string.
     *
     * @param context Type of context used
     * @return String this packet as a string
     */
    public String toString(VentisCodec context) {
        return this.getClass().getName() + Connection.SPLIT_REGEX + context.serialize(this);
    }
}