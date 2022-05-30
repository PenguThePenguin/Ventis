package me.pengu.ventis.codec.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.pengu.ventis.codec.VentisCodec;
import me.pengu.ventis.packet.Packet;

import java.util.function.Supplier;

/**
 * A Gson (https://github.com/google/gson)
 * adapter of {@link VentisCodec}
 */
public class GsonCodec implements VentisCodec {

    private Gson gson = new GsonBuilder().serializeNulls().create();

    public GsonCodec provideGson(Supplier<Gson> gson) {
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
