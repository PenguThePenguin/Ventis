package me.pengu.ventis.connection.implementation.rabbitmq;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import me.pengu.ventis.Ventis;
import me.pengu.ventis.connection.Connection;
import me.pengu.ventis.connection.config.RabbitMQConfig;
import me.pengu.ventis.packet.Packet;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

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

    public RabbitMQConnection(Ventis ventis) {
        super(ventis, "RabbitMQ");

        this.rabbitMQConfig = ventis.getConfig().getRabbitMQConfig();
        this.routingKey = Connection.CHANNEL_PREFIX + this.ventis.getConfig().getChannel();

        this.subscriber = new RabbitMQSubscriber(this);
        this.ventis.getExecutor().submit(this::connect);
    }

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

    @Override
    public CompletableFuture<Void> sendPacket(Packet packet, String channel) {
        return CompletableFuture.runAsync(() -> {
            if (!this.isConnected()) return;

            try {
                ByteArrayDataOutput output = ByteStreams.newDataOutput();
                output.writeUTF(packet.toString(this.config.getContext()));
                this.channel.basicPublish(EXCHANGE_NAME, this.routingKey, new BasicProperties.Builder().build(), output.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, this.getVentis().getExecutor());
    }

    @Override
    public void close() {
        try {
            this.channel.close();
            this.connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.close();
    }
}
