package me.pengu.ventis.messenger.impl.sql;

import me.pengu.ventis.Ventis;
import me.pengu.ventis.messenger.Messenger;
import me.pengu.ventis.VentisConfig;
import me.pengu.ventis.packet.Packet;

import java.util.concurrent.CompletableFuture;

// TODO
public class SqlMessenger extends Messenger {

    public SqlMessenger(Ventis ventis) {
        super(ventis);
    }

    @Override
    public CompletableFuture<Void> sendPacket(Packet packet, String channel) {
        return null;
    }
}
