package me.pengu.ventis.connection.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.pengu.ventis.connection.implementation.sql.connection.SqlConnection;
import me.pengu.ventis.connection.implementation.sql.connection.hikari.MySqlConnection;

/**
 * Sql Config.
 * Provides {@link SqlConnection} with the provided options
 */
@Getter @Builder
public class SqlConfig {

    @Default private SqlConnection connection = new MySqlConnection();

    private String address;
    private String database;

    private String username;
    private String password;

}
