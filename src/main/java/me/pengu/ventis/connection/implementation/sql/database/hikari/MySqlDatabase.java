package me.pengu.ventis.connection.implementation.sql.database.hikari;

import com.zaxxer.hikari.HikariConfig;

public class MySqlDatabase extends HikariDatabse {

    @Override
    protected void configureConfig(HikariConfig config, String address, String port, String databaseName, String username, String password) {
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + databaseName);
        config.setUsername(username);
        config.setPassword(password);
    }
}