package me.pengu.ventis.connection.implementation.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Future;

public class SocketServer implements Runnable {

    private final SocketConnection connection;
    private ServerSocket serverSocket;

    private final Future<?> runnable;

    public SocketServer(SocketConnection connection) {
        this.connection = connection;
        this.setupSocket();

        this.runnable = this.connection.getVentis().getExecutor().submit(this);
    }

    private void setupSocket() {
        try {
            this.serverSocket = new ServerSocket(this.connection.getSocketConfig().getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (this.connection.isConnected()) {
            try (Socket socket = serverSocket.accept();
                 DataInputStream input = new DataInputStream(socket.getInputStream())) {

                String channel = input.readUTF();
                if (!this.connection.getChannel().equals(channel)) return;

                String message = input.readUTF();

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

                this.connection.handleMessage(channel, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        if (!this.runnable.isCancelled()) this.runnable.cancel(true);

        if (this.serverSocket != null && !this.serverSocket.isClosed()) {
            try {
                this.serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}