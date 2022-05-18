package me.pengu.ventis.connection.implementation.sql.connection;

import me.pengu.ventis.connection.config.SqlConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Sql Connection
 * A interface for sql database types to implement.
 */
public interface SqlConnection {

    /**
     * Loads connection from config
     *
     * @param config  sql config to load from
     */
    default void load(SqlConfig config) { }

    /**
     * This active connection
     *
     * @return the sql connection
     * @throws SQLException when the database isn't active
     */
    Connection getConnection() throws SQLException;

    /**
     * Cleans up this connection instance.
     *
     * @see SqlConnection#close()
     */
    void close();

}