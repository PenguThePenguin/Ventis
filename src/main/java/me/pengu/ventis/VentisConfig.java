package me.pengu.ventis;

import lombok.Builder;
import lombok.Getter;
import me.pengu.ventis.context.VentisContext;
import me.pengu.ventis.context.impl.GsonContext;

/**
 * Ventis Config.
 * Provides {@link Ventis} with the provided options
 */
@Getter @Builder
public class VentisConfig {

    @Builder.Default private VentisContext context = new GsonContext();

    private String channel;
    private String address;
    private int port;

    private boolean auth;
    private String password;
}
