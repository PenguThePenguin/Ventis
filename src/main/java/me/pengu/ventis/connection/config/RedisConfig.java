package me.pengu.ventis.connection.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.pengu.ventis.connection.implementation.redis.RedisConnection;
import redis.clients.jedis.Protocol;

/**
 * Redis Config.
 * Provides {@link RedisConnection} with the provided options
 */
@Getter @Builder
public class RedisConfig {

    @Default private int timeout = Protocol.DEFAULT_TIMEOUT;
    @Default private String address = "127.0.0.1";
    @Default private int port = 6379;

    private boolean auth;
    private String password;

}
