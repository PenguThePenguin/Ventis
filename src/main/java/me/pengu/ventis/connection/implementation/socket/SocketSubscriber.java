package me.pengu.ventis.connection.implementation.socket;

import me.pengu.ventis.Ventis;
import me.pengu.ventis.connection.implementation.redis.RedisConnection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Future;

/**
 * Socket Subscriber
 * Implements {@link Runnable} for a task to check for data.
 */
public class SocketSubscriber implements Runnable {

    private final SocketConnection connection;
    private final Future<?> runnable;

    /**
     * Socket Subscriber instance.
     * Initializes this as-well as submitting this to {@link Ventis}'s executor.
     *
     * @param connection {@link SocketConnection} instance
     */
    public SocketSubscriber(SocketConnection connection) {
        this.connection = connection;
        this.runnable = this.connection.getVentis().getExecutor().submit(this);
    }

    /**
     * An implementation of {@link Runnable#run()}.
     */
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

    /**
     * Cleans up this subscriber instance.
     */
    public void close() {
        if (!this.runnable.isCancelled()) this.runnable.cancel(true);
    }
}