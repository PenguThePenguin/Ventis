package me.pengu.ventis.connection.implementation.socket;

import lombok.Getter;
import me.pengu.ventis.Ventis;
import me.pengu.ventis.connection.Connection;
import me.pengu.ventis.packet.Packet;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

@Getter
public class SocketConnection extends Connection {

    private final String channel;
    private final SocketServer socketServer;

    public SocketConnection(Ventis ventis) {
        super(ventis);

        this.channel = Connection.CHANNEL_PREFIX + this.ventis.getConfig().getChannel();
        this.socketServer = new SocketServer(this);
    }

    @Override
    public CompletableFuture<Void> sendPacket(Packet packet, String channel) {
        return CompletableFuture.runAsync(() -> {
            if (!this.isConnected()) return;

            for (Server server : this.ventis.getConfig().getSocketConfig().getServers()) {
                try (Socket socket = new Socket(InetAddress.getByName(server.getAddress()), server.getPort());
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                    out.writeUTF(this.channel);
                    out.writeUTF(packet.toString(this.config.getContext()));
                    if (this.config.getSocketConfig().isAuth()) out.writeUTF(this.config.getSocketConfig().getPassword());

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
