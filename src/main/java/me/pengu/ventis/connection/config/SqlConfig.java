package me.pengu.ventis.connection.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import me.pengu.ventis.connection.implementation.sql.database.SqlDatabase;
import me.pengu.ventis.connection.implementation.sql.database.hikari.HikariDatabse;
import me.pengu.ventis.connection.implementation.sql.database.hikari.MySqlDatabase;
import org.jetbrains.annotations.NotNull;

/**
 * Sql Config.
 * Provides {@link SqlDatabase} with the provided options
 */
@Getter @Builder
public class SqlConfig {

    @NotNull @Default private SqlDatabase database = new MySqlDatabase();

    private String databaseName;
    private String address;
    private String port;

    private String username;
    private String password;

    // host:port
    public SqlConfig fromConnectionString(String connectionString) {
        String[] addressSplit = connectionString.split(":");

        String address = addressSplit[0];
        String port = addressSplit.length > 1 ? addressSplit[1] :
                this.database instanceof HikariDatabse
                        ? ((HikariDatabse) this.database).getDefaultPort() : "1433";

        this.address = address;
        this.port = port;

        return this;
    }

}
