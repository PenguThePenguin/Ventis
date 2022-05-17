package me.pengu.ventis.connection.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.pengu.ventis.connection.implementation.redis.RedisConnection;
import redis.clients.jedis.Protocol;

/**
 * RabbitMQ Config.
 * Provides {@link RedisConnection} with the provided options
 */
@Getter @Builder
public class RabbitMQConfig {

    @Default private String address = "127.0.0.1";
    @Default private int port = 5672;
    private String virtualHost;

    private String username;
    private String password;

}
