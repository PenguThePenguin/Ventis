package me.pengu.ventis.connection.implementation.socket.data;

import lombok.Data;
import me.pengu.ventis.connection.implementation.socket.SocketConnection;
import me.pengu.ventis.packet.Packet;

import java.net.InetAddress;

/**
 * Represents a server that packets can be sent to.
 * @see SocketConnection#sendPacket(Packet, String)
 */
@Data
public class Server {

    private String address;
    private int port;

    private InetAddress inetAddress;

    public Server(String address, int port) {
        this.address = address;
        this.port = port;

        this.inetAddress = this.findInetAddress();
    }

    private InetAddress findInetAddress() {
        try {
            return InetAddress.getByName(this.address);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Failed to to find a server with address %1$s, is it down? (%2$s)",
                            this.address, e.getMessage()));
        }
    }
}