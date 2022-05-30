package me.pengu.ventis.connection.implementation.sql.database.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.pengu.ventis.connection.config.SqlConfig;
import me.pengu.ventis.connection.implementation.sql.database.SqlDatabase;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class HikariDatabse implements SqlDatabase {

    public SqlConfig config;

    private HikariDataSource hikari;
    private final HikariConfig hikariConfig;

    public HikariDatabse() {
        this.hikariConfig = new HikariConfig();
        this.hikariConfig.setPoolName("ventis-hikari");
    }

    @Override
    public void load(SqlConfig config) {
        this.config = config;

        this.configureConfig(
                hikariConfig, this.config.getAddress(), this.config.getPort(), this.config.getDatabaseName(),
                this.config.getUsername(), this.config.getPassword()
        );

        this.hikari = new HikariDataSource(this.hikariConfig);
    }

    protected abstract void configureConfig(HikariConfig config, String address, String port, String databaseName, String username, String password);

    public String getDefaultPort() {
        return "3306";
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (this.hikari == null) {
            throw new SQLException("Unable to get a connection from the pool. (hikari is null)");
        }

        return this.hikari.getConnection();
    }

    @Override
    public void close() {
        if (this.hikari != null) {
            this.hikari.close();
        }
    }
}