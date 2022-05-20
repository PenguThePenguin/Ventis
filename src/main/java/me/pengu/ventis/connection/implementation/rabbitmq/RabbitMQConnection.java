package me.pengu.ventis.connection.implementation.rabbitmq;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import me.pengu.ventis.Ventis;
import me.pengu.ventis.connection.Connection;
import me.pengu.ventis.connection.config.RabbitMQConfig;
import me.pengu.ventis.packet.Packet;

import java.util.concurrent.CompletableFuture;

/**
 * RabbitMQ Connection
 * Extends {@link Connection} for packet handling.
 */
@Getter
public class RabbitMQConnection extends Connection {

    private static final String EXCHANGE_NAME = "ventis";
    private static final boolean DURABLE = false;
    private static final boolean EXCLUSIVE = true;
    private static final boolean AUTO_DELETE = true;

    private final RabbitMQSubscriber subscriber;
    private final RabbitMQConfig rabbitMQConfig;
    private final String routingKey;

    private ConnectionFactory connectionFactory;
    private com.rabbitmq.client.Connection connection;
    private Channel channel;

    /**
     * RabbitMQ Connection instance.
     * @param ventis {@link Ventis} instance
     * @param rabbitMQConfig the provided options for this connection
     */
    public RabbitMQConnection(Ventis ventis, RabbitMQConfig rabbitMQConfig) {
        super(ventis, "rabbitmq");

        this.rabbitMQConfig = rabbitMQConfig;
        this.routingKey = CHANNEL_PREFIX + this.ventis.getConfig().getChannel();

        this.subscriber = new RabbitMQSubscriber(this);
        this.connect();
    }

    /**
     * Sets up this connection to RabbitMQ
     */
    private void connect() {
        try {
            this.connectionFactory = new ConnectionFactory();
            this.connectionFactory.setHost(this.rabbitMQConfig.getAddress());
            this.connectionFactory.setPort(this.rabbitMQConfig.getPort());
            this.connectionFactory.setVirtualHost(this.rabbitMQConfig.getVirtualHost());
            this.connectionFactory.setUsername(this.rabbitMQConfig.getUsername());
            this.connectionFactory.setPassword(this.rabbitMQConfig.getPassword());

            this.connection = connectionFactory.newConnection();
            this.channel = this.connection.createChannel();

            String queue = this.channel.queueDeclare("", DURABLE, EXCLUSIVE, AUTO_DELETE, null).getQueue();
            this.channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, DURABLE, AUTO_DELETE , null);
            this.channel.queueBind(queue, EXCHANGE_NAME, this.routingKey);
            this.channel.basicConsume(queue, true, this.subscriber, tag -> {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a packet.
     *
     * @param packet  packet to send
     * @param channel rabbitMQ channel to use
     * @return a future to manipulate the result of the operation
     */
    @Override
    public CompletableFuture<Void> sendPacket(Packet packet, String channel) {
        return CompletableFuture.runAsync(() -> {
            if (!this.isConnected()) return;

            ByteArrayDataOutput output = ByteStreams.newDataOutput();

            output.writeUTF(CHANNEL_PREFIX + channel);
            output.writeUTF(packet.toString(this.config.getContext()));

            try {
                this.channel.basicPublish(EXCHANGE_NAME, this.routingKey, new BasicProperties.Builder().build(), output.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, this.getVentis().getExecutor());
    }

    /**
     * Cleans up this socket instance.
     * @see Connection#close()
     */
    @Override
    public void close() {
        try {
            if (this.channel.isOpen()) this.channel.close();
            if (this.connection.isOpen()) this.connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.close();
    }
}
