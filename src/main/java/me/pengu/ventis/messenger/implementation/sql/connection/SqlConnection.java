package me.pengu.ventis.messenger.implementation.sql.connection;

import me.pengu.ventis.messenger.config.SqlConfig;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlConnection {

    default void load(SqlConfig config) { }

    Connection getConnection() throws SQLException;

    void close();

}
