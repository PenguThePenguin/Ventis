package me.pengu.ventis.codec.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.pengu.ventis.codec.VentisCodec;
import me.pengu.ventis.packet.Packet;

import java.util.function.Supplier;

/**
 * A Jackson (https://github.com/FasterXML/jackson)
 * adapter of {@link VentisCodec}
 */
public class JacksonCodec implements VentisCodec {

    private ObjectMapper mapper = new ObjectMapper();

    public JacksonCodec provideMapper(Supplier<ObjectMapper> mapper) {
        this.mapper = mapper.get();
        return this;
    }

    @Override
    public String serialize(Packet packet) {
        try {
            return this.mapper.writeValueAsString(packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Packet deSerialize(String data, Class<? extends Packet> packetClass) {
        return this.mapper.convertValue(data, packetClass);
    }
}