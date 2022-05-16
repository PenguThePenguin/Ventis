package me.pengu.ventis.messenger.implementation.sql.connection.hikari;

import com.zaxxer.hikari.HikariConfig;

public class PostgreConnection extends HikariConnection {

    @Override
    protected void configureConfig(HikariConfig config, String address, String port, String databaseName, String username, String password) {
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.addDataSourceProperty("serverName", address);
        config.addDataSourceProperty("portNumber", port);
        config.addDataSourceProperty("databaseName", databaseName);
        config.addDataSourceProperty("user", username);
        config.addDataSourceProperty("password", password);
    }

    @Override
    protected String getDefaultPort() {
        return "5432";
    }
}
