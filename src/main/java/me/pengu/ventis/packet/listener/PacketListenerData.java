package me.pengu.ventis.packet.listener;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents the data of a {@link PacketListener}
 */
@Data
public class PacketListenerData {

    private final PacketListener instance;
    private final Method method;

    private final List<String> channels;

}