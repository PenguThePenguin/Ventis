package me.pengu.ventis.context.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Setter;
import me.pengu.ventis.context.VentisContext;
import me.pengu.ventis.packet.Packet;

import java.util.function.Supplier;

/**
 * A Gson (https://github.com/google/gson)
 * adapter of {@link VentisContext}
 */
public class GsonContext implements VentisContext {

    private Gson gson = new GsonBuilder().serializeNulls().create();

    public GsonContext provideGson(Supplier<Gson> gson) {
        this.gson = gson.get();
        return this;
    }

    @Override
    public String serialize(Packet packet) {
        return this.gson.toJson(packet);
    }

    @Override
    public Packet deSerialize(String data, Class<? extends Packet> packetClass) {
        return this.gson.fromJson(data, packetClass);
    }
}
