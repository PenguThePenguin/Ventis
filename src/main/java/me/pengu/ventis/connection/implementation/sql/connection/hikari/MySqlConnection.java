package me.pengu.ventis.connection.implementation.sql.connection.hikari;

import com.zaxxer.hikari.HikariConfig;

public class MySqlConnection extends HikariConnection {

    @Override
    protected void configureConfig(HikariConfig config, String address, String port, String databaseName, String username, String password) {
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + databaseName);
        config.setUsername(username);
        config.setPassword(password);
    }
}