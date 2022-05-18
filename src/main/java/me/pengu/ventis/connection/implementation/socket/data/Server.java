package me.pengu.ventis.connection.implementation.socket.data;

import lombok.Data;
import me.pengu.ventis.connection.Connection;
import me.pengu.ventis.connection.implementation.socket.SocketConnection;
import me.pengu.ventis.packet.Packet;

/**
 * Represents a server that packets can be sent to.
 * @see SocketConnection#sendPacket(Packet, String)
 */
@Data
public class Server {

    private String address;
    private int port;

}