package me.pengu.ventis.packet;

import lombok.Data;
import me.pengu.ventis.context.VentisContext;
import me.pengu.ventis.messenger.Messenger;

/**
 * Represents data that can be sent through redis.
 * @see Messenger#sendPacket(Packet)
 */
@Data
public class Packet {

    /**
     * The name of this class,
     * here to make the context serialize it.
     */
    private final String className = this.getClass().getName();

    /**
     * Gets this packet as a string.
     * @param context Type of context used
     * @return String this packet as a string
     */
    public String toString(VentisContext context) {
        return this.getClassName() + Messenger.SPLIT_REGEX + context.serialize(this);
    }
}