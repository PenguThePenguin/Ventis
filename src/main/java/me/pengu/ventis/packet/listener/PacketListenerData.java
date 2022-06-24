package me.pengu.ventis.packet.listener;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * Represents the data of a {@link PacketListener}
 */
@Data
public class PacketListenerData {

    private final PacketListener instance;
    private final Method method;

    private final String[] channels;

}