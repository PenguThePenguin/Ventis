package me.pengu.ventis.connection.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.pengu.ventis.connection.implementation.redis.RedisConnection;
import me.pengu.ventis.connection.implementation.socket.Server;

import java.util.ArrayList;
import java.util.List;

/**
 * Socket Config.
 * Provides {@link RedisConnection} with the provided options
 */
@Getter @Builder
public class SocketConfig {

    @Default List<Server> servers = new ArrayList<>();

    private String address;
    private int port;

    private boolean auth;
    private String password;

}