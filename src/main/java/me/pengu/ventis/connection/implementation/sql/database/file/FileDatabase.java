package me.pengu.ventis.connection.implementation.sql.database.file;

import me.pengu.ventis.connection.implementation.sql.database.SqlDatabase;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class FileDatabase implements SqlDatabase {

    private final Path file;
    private ConnectionWrapper connection;

    public FileDatabase(Path file) {
        this.file = file;

        this.loadConnection();
    }

    public abstract void loadConnection();

    public abstract Connection createConnection(Path file) throws SQLException;

    @Override
    public synchronized Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connection = new ConnectionWrapper(this.createConnection(this.file));
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