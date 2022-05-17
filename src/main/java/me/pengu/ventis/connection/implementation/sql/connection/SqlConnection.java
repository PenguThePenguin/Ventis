package me.pengu.ventis.connection.implementation.sql.connection;

import me.pengu.ventis.connection.config.SqlConfig;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlConnection {

    default void load(SqlConfig config) { }

    Connection getConnection() throws SQLException;

    void close();

}