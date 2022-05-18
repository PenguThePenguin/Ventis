package me.pengu.ventis;

import com.sun.istack.internal.NotNull;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.pengu.ventis.context.VentisContext;
import me.pengu.ventis.context.impl.GsonContext;

/**
 * Ventis Config.
 * Provides {@link Ventis} with the provided options
 */
@Getter @Builder
public class VentisConfig {

    @Default private VentisContext context = new GsonContext();
    @NotNull private String channel;

}