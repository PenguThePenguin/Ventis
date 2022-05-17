package me.pengu.ventis.connection.implementation.sql.connection.file;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class SqLiteConnection extends FileConnection {

    private Constructor<?> connectionConstructor;

    public SqLiteConnection(Path file) {
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
    public Connection createConnection(Path file) {
        try {
            return (Connection) this.connectionConstructor.newInstance("jdbc:sqlite:" + file.toString(), file.toString(), new Properties());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
