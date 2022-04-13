package me.pengu.ventis.context.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.pengu.ventis.context.VentisContext;
import me.pengu.ventis.packet.Packet;

/**
 * A Gson (https://github.com/google/gson)
 * adapter of {@link VentisContext}
 */
public class GsonContext implements VentisContext {

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public String serialize(Packet packet) {
        return this.gson.toJson(packet);
    }

    @Override
    public Packet deSerialize(String data, Class<? extends Packet> packetClass) {
        return this.gson.fromJson(data, packetClass);
    }
}
