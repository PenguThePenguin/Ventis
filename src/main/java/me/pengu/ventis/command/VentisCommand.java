package me.pengu.ventis.command;

import me.pengu.ventis.Ventis;
import redis.clients.jedis.Jedis;

/**
 * Ventis Command.
 * A callback when executing a command on the jedisPool.
 * @see Ventis#runCommand(VentisCommand)
 * @param <Data> The generic type to return.
 */
public interface VentisCommand<Data> {

    /**
     * Executes the specified command.
     * @param jedis the jedis to execute on.
     * @return Data the generic type.
     */
    Data execute(Jedis jedis);

}

