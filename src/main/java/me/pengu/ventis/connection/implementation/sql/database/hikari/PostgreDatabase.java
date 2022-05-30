package me.pengu.ventis.connection.implementation.sql.database.hikari;

import com.zaxxer.hikari.HikariConfig;

public class PostgreDatabase extends HikariDatabse {

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
    public String getDefaultPort() {
        return "5432";
    }
}
