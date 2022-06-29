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
     * The channel this packet was sent on
     */
    private String chanel;

    /**
     * Gets this packet as a string.
     *
     * @param channel the channel this was sent on
     * @param codec   Type of codec used
     * @return this packet as a string
     */
    public String toString(String channel, VentisCodec codec) {
        this.chanel = channel;

        return this.getClass().getSimpleName().toLowerCase() + Connection.SPLIT_REGEX + codec.serialize(this);
    }
}