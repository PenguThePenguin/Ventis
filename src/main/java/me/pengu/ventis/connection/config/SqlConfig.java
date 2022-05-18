package me.pengu.ventis.connection.config;

import com.sun.istack.internal.NotNull;
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

    @NotNull @Default private SqlConnection connection = new MySqlConnection();

    private String address;
    private String database;

    private String username;
    private String password;

}
