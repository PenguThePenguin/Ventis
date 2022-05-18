package me.pengu.ventis.connection.implementation.rabbitmq;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RabbitMQSubscriber implements DeliverCallback {

    private final RabbitMQConnection connection;

    @Override
    public void handle(String consumerTag, Delivery delivery) {
        ByteArrayDataInput input = ByteStreams.newDataInput(delivery.getBody());

        String channel = input.readUTF();
        String packet = input.readUTF();

        this.connection.handleMessage(channel, packet);
    }
}
