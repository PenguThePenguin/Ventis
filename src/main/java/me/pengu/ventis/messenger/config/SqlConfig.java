package me.pengu.ventis.messenger.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.pengu.ventis.messenger.implementation.sql.SqlMessenger;
import me.pengu.ventis.messenger.implementation.sql.connection.SqlConnection;
import me.pengu.ventis.messenger.implementation.sql.connection.hikari.MySqlConnection;

import java.util.Map;

/**
 * Sql Config.
 * Provides {@link SqlMessenger} with the provided options
 */
@Getter @Builder
public class SqlConfig {

    @Default private SqlConnection connection = new MySqlConnection();

    private String address;
    private String database;

    private String username;
    private String password;

}
