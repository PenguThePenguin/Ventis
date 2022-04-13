package me.pengu.ventis;

import lombok.Builder;
import lombok.Getter;
import lombok.Builder.Default;
import me.pengu.ventis.context.VentisContext;
import me.pengu.ventis.context.impl.GsonContext;

/**
 * Ventis Config.
 * Provides {@link Ventis} with the provided options
 */
@Getter @Builder
public class VentisConfig {

    @Default private VentisContext context = new GsonContext();

    private String channel;
    @Default private String address = "127.0.0.1";
    @Default private int port = 6379;

    private boolean auth;
    private String password;
}
