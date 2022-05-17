package me.pengu.ventis;

import com.sun.istack.internal.NotNull;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.pengu.ventis.connection.config.RabbitMQConfig;
import me.pengu.ventis.context.VentisContext;
import me.pengu.ventis.context.impl.GsonContext;
import me.pengu.ventis.connection.config.RedisConfig;
import me.pengu.ventis.connection.config.SocketConfig;
import me.pengu.ventis.connection.config.SqlConfig;

/**
 * Ventis Config.
 * Provides {@link Ventis} with the provided options
 */
@Getter @Builder
public class VentisConfig {

    @Default private String connectionType = "redis";

    @Default private VentisContext context = new GsonContext();
    @NotNull private String channel;

    private RedisConfig redisConfig;
    private SqlConfig sqlConfig;
    private SocketConfig socketConfig;
    private RabbitMQConfig rabbitMQConfig;

}