package me.pengu.ventis.connection.implementation.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Future;

public class SocketSubscriber implements Runnable {

    private final SocketConnection connection;
    private final Future<?> runnable;

    public SocketSubscriber(SocketConnection connection) {
        this.connection = connection;
        this.runnable = this.connection.getVentis().getExecutor().submit(this);
    }

    @Override
    public void run() {
        while (this.connection.isConnected()) {
            try (Socket socket = this.connection.getSocket().accept();
                 DataInputStream input = new DataInputStream(socket.getInputStream())) {

                String socketPrefix = input.readUTF();
                if (!this.connection.getConfig().getChannel().equals(socketPrefix)) return;

                String channel = input.readUTF();
                String packet = input.readUTF();

                if (this.connection.getSocketConfig().isAuth()) {
                    String password = input.readUTF();

                    if (!password.equals(this.connection.getSocketConfig().getPassword())) {
                        throw new RuntimeException(
                                "Attempted Un-authenticated packet sent on channel " + channel + ":\n" +
                                        "Address: " + socket.getInetAddress().getHostAddress() + "\n" +
                                        "Hostname: " + socket.getInetAddress().getHostName() + "\n" +
                                        "Port: " + socket.getPort() + "\n" +
                                        "Password: " + (password.isEmpty() ? "none" : password) + "\n"
                        );
                    }
                }

                this.connection.handleMessage(channel, packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        if (!this.runnable.isCancelled()) this.runnable.cancel(true);
    }
}