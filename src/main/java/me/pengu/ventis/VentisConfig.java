package me.pengu.ventis;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.pengu.ventis.codec.VentisCodec;
import me.pengu.ventis.codec.impl.GsonCodec;
import org.jetbrains.annotations.NotNull;

/**
 * Ventis Config.
 * Provides {@link Ventis} with the provided options
 */
@Getter @Builder
public class VentisConfig {

    @Default private VentisCodec codex = new GsonCodec();
    @NotNull private String channel;

}