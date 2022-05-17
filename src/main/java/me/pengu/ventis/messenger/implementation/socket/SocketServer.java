package me.pengu.ventis.messenger.implementation.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Future;

public class SocketServer implements Runnable {

    private final SocketMessenger messenger;
    private ServerSocket serverSocket;

    private final Future<?> runnable;

    public SocketServer(SocketMessenger messenger) {
        this.messenger = messenger;
        this.setupSocket();

        this.runnable = this.messenger.getVentis().getExecutor().submit(this);
    }

    private void setupSocket() {
        try {
            this.serverSocket = new ServerSocket(this.messenger.getConfig().getSocketConfig().getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (this.messenger.isConnected()) {
            try (Socket socket = serverSocket.accept(); DataInputStream input = new DataInputStream(socket.getInputStream())) {

                String channel = input.readUTF();
                if (!this.messenger.getChannel().equals(channel)) return;

                String message = input.readUTF();

                if (this.messenger.getConfig().getSocketConfig().isAuth()) {
                    String password = input.readUTF();

                    if (!password.equals(this.messenger.getConfig().getSocketConfig().getPassword())) {
                        throw new RuntimeException(
                                "Attempted Un-authenticated packet sent on channel " + channel + ":\n" +
                                        "Address: " + socket.getInetAddress().getHostAddress() + "\n" +
                                        "Hostname: " + socket.getInetAddress().getHostName() + "\n" +
                                        "Port: " + socket.getPort() + "\n" +
                                        "Password: " + (password.isEmpty() ? "none" : password) + "\n"
                        );
                    }
                }

                this.messenger.handleMessage(channel, message);
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