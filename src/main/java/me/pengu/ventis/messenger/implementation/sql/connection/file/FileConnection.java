package me.pengu.ventis.messenger.implementation.sql.connection.file;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.pengu.ventis.messenger.config.SqlConfig;
import me.pengu.ventis.messenger.implementation.sql.connection.SqlConnection;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
public abstract class FileConnection implements SqlConnection {

    private ConnectionWrapper connection;
    private final Path file;

    public abstract void loadConnection();
    public abstract Connection createConnection(Path file) throws SQLException;

    @Override
    public Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connection = new ConnectionWrapper(createConnection(this.file));
        }
        return connection;
    }

    @Override
    public void close() {
        if (this.connection != null) {
            try {
                this.connection.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
