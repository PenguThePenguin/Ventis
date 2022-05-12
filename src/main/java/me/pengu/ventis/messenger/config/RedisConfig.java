package me.pengu.ventis.messenger.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.pengu.ventis.messenger.impl.redis.RedisMessenger;

/**
 * Redis Config.
 * Provides {@link RedisMessenger} with the provided options
 */
@Getter @Builder
public class RedisConfig {

    private String channel;
    @Default private String address = "127.0.0.1";
    @Default private int port = 6379;

    private boolean auth;
    private String password;

}
