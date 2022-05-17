package me.pengu.ventis.connection.implementation.sql.connection.hikari;

import com.zaxxer.hikari.HikariConfig;

public class MariaDbConnection extends HikariConnection {

    @Override
    protected void configureConfig(HikariConfig config, String address, String port, String databaseName, String username, String password) {
        config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        config.addDataSourceProperty("serverName", address);
        config.addDataSourceProperty("port", port);
        config.addDataSourceProperty("databaseName", databaseName);
        config.setUsername(username);
        config.setPassword(password);
    }
}
