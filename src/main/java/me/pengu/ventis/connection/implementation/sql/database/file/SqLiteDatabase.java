package me.pengu.ventis.connection.implementation.sql.database.file;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class SqLiteDatabase extends FileDatabase {

    private Constructor<?> connectionConstructor;

    public SqLiteDatabase(Path file) {
        super(file);
    }

    @Override
    public void loadConnection() {
        try {
            Class<?> connectionClass = Class.forName("org.sqlite.jdbc4.JDBC4Connection");
            this.connectionConstructor = connectionClass.getConstructor(String.class, String.class, Properties.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection createConnection(Path file) throws SQLException {
        try {
            return (Connection) this.connectionConstructor.newInstance("jdbc:sqlite:" + file.toString(), file.toString(), new Properties());
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}
