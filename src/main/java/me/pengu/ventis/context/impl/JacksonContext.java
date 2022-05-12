package me.pengu.ventis.context.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.pengu.ventis.context.VentisContext;
import me.pengu.ventis.packet.Packet;

import java.util.function.Supplier;

/**
 * A Jackson (https://github.com/FasterXML/jackson)
 * adapter of {@link VentisContext}
 */
public class JacksonContext implements VentisContext {

    private ObjectMapper mapper = new ObjectMapper();

    public JacksonContext provideMapper(Supplier<ObjectMapper> mapper) {
        this.mapper = mapper.get();
        return this;
    }

    @Override
    public String serialize(Packet packet) {
        String serialized = "";
        try {
            serialized = this.mapper.writeValueAsString(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serialized;
    }

    @Override
    public Packet deSerialize(String data, Class<? extends Packet> packetClass) {
        return this.mapper.convertValue(data, packetClass);
    }
}
