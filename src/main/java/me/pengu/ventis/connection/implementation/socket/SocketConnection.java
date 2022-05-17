package me.pengu.ventis.connection.implementation.socket;

import lombok.Getter;
import me.pengu.ventis.Ventis;
import me.pengu.ventis.connection.Connection;
import me.pengu.ventis.connection.config.SocketConfig;
import me.pengu.ventis.packet.Packet;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

@Getter
public class SocketConnection extends Connection {

    private final SocketConfig socketConfig;
    private final SocketServer socketServer;
    private final String channel;

    public SocketConnection(Ventis ventis, SocketConfig socketConfig) {
        super(ventis, "socket");
        this.socketConfig = socketConfig;

        this.channel = Connection.CHANNEL_PREFIX + this.ventis.getConfig().getChannel();
        this.socketServer = new SocketServer(this);
    }

    @Override
    public CompletableFuture<Void> sendPacket(Packet packet, String channel) {
        return CompletableFuture.runAsync(() -> {
            if (!this.isConnected()) return;

            for (Server server : this.socketConfig.getServers()) {
                try (Socket socket = new Socket(InetAddress.getByName(server.getAddress()), server.getPort());
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                    out.writeUTF(this.channel);
                    out.writeUTF(packet.toString(this.config.getContext()));
                    if (this.socketConfig.isAuth()) out.writeUTF(this.socketConfig.getPassword());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, this.ventis.getExecutor());
    }


    @Override
    public void close() {
        this.socketServer.close();

        super.close();
    }
}
