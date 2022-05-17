package me.pengu.ventis.messenger.implementation.sql.connection.file;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class H2Connection extends FileConnection {

    private Constructor<?> connectionConstructor;

    public H2Connection(Path file) {
        super(file);
    }

    @Override
    public void loadConnection() {
        try {
            Class<?> connectionClass = Class.forName("org.h2.jdbc.JdbcConnection");
            this.connectionConstructor = connectionClass.getConstructor(String.class, Properties.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection createConnection(Path file) {
        try {
            return (Connection) this.connectionConstructor.newInstance("jdbc:h2:" + file.toString(), new Properties());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
