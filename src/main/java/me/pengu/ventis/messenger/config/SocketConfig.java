package me.pengu.ventis.messenger.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.pengu.ventis.messenger.implementation.redis.RedisMessenger;
import me.pengu.ventis.messenger.implementation.socket.Server;

import java.util.ArrayList;
import java.util.List;

/**
 * Socket Config.
 * Provides {@link RedisMessenger} with the provided options
 */
@Getter @Builder
public class SocketConfig {

    @Default List<Server> servers = new ArrayList<>();

    private String address;
    private int port;

    private boolean auth;
    private String password;

}