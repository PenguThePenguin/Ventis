package me.pengu.ventis.packet.handler;

import me.pengu.ventis.VentisSubscriber;
import me.pengu.ventis.packet.listener.PacketListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for each packet in {@link PacketListener}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketHandler {

    /**
     * This provides the {@link VentisSubscriber}
     * an array of channels to listen for.
     */
    String[] channels() default {};

}
