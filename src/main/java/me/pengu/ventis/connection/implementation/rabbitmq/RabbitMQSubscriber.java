package me.pengu.ventis.connection.implementation.rabbitmq;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

public class RabbitMQSubscriber implements DeliverCallback {

    private final RabbitMQConnection connection;

    public RabbitMQSubscriber(RabbitMQConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handle(String consumerTag, Delivery delivery) {
        byte[] data = delivery.getBody();

        ByteArrayDataInput input = ByteStreams.newDataInput(data);
        String message = input.readUTF();

        this.connection.handleMessage(consumerTag, message);
    }
}
