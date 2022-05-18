package me.pengu.ventis.connection.implementation.socket;

import lombok.Getter;
import me.pengu.ventis.Ventis;
import me.pengu.ventis.connection.Connection;
import me.pengu.ventis.connection.config.SocketConfig;
import me.pengu.ventis.connection.implementation.socket.data.Server;
import me.pengu.ventis.packet.Packet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

@Getter
public class SocketConnection extends Connection {

    private final SocketConfig socketConfig;
    private final String socketPrefix;

    private final ServerSocket socket;
    private final SocketSubscriber subscriber;

    public SocketConnection(Ventis ventis, SocketConfig socketConfig) {
        super(ventis, "socket");

        this.socketConfig = socketConfig;
        this.socketPrefix = CHANNEL_PREFIX + this.ventis.getConfig().getChannel();

        this.socket = this.setupSocket();
        this.subscriber = new SocketSubscriber(this);
    }

    private ServerSocket setupSocket() {
        try {
            return new ServerSocket(this.getSocketConfig().getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<Void> sendPacket(Packet packet, String channel) {
        return CompletableFuture.runAsync(() -> {
            if (!this.isConnected()) return;

            String data = packet.toString(this.config.getContext());

            for (Server server : this.socketConfig.getServers()) {
                try (Socket socket = new Socket(InetAddress.getByName(server.getAddress()), server.getPort());
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                    out.writeUTF(this.socketPrefix); // As we are not subscribing to a specific channel.
                    out.writeUTF(CHANNEL_PREFIX + channel);
                    out.writeUTF(data);
                    if (this.socketConfig.isAuth()) out.writeUTF(this.socketConfig.getPassword());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, this.ventis.getExecutor());
    }


    @Override
    public void close() {
        this.subscriber.close();

        try {
            if (!this.socket.isClosed()) this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.close();
    }
}
