package me.pengu.ventis.connection.implementation.sql;

import lombok.Getter;
import lombok.Setter;
import me.pengu.ventis.Ventis;
import me.pengu.ventis.connection.Connection;
import me.pengu.ventis.connection.config.SqlConfig;
import me.pengu.ventis.connection.implementation.sql.tasks.SqlCheckMessagesTask;
import me.pengu.ventis.connection.implementation.sql.tasks.SqlCleanupTask;
import me.pengu.ventis.packet.Packet;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Sql Connection
 * Extends {@link Connection} for packet handling.
 */
@Getter @Setter
public class SqlConnection extends Connection {

    private final SqlConfig sqlConfig;
    private final String tableName;

    private final SqlCleanupTask cleanupTask;
    private final SqlCheckMessagesTask messagesTask;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private long lastId = 0;

    /**
     * Sql Connection instance.
     *
     * @param ventis {@link Ventis} instance
     */
    public SqlConnection(Ventis ventis, SqlConfig sqlConfig) {
        super(ventis, "sql");

        this.sqlConfig = sqlConfig;
        this.tableName = Connection.CHANNEL_PREFIX + this.ventis.getConfig().getChannel();

        this.sqlConfig.getConnection().load(this.sqlConfig);

        this.cleanupTask = new SqlCleanupTask(this);
        this.messagesTask = new SqlCheckMessagesTask(this);
    }

    /**
     * Sends a packet.
     *
     * @param packet  packet to send
     * @param channel redis channel to use
     * @return a future to manipulate the result of the operation
     */
    @Override
    public CompletableFuture<Void> sendPacket(Packet packet, String channel) {
        return CompletableFuture.runAsync(() -> {
            if (this.checkLock()) return;

            try (java.sql.Connection connection = getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + getTableName() + "` (`time`, 'channel' `message`) VALUES(NOW(), ?, ?)")) {
                    ps.setString(1, packet.toString(this.config.getContext()));
                    ps.setString(2, channel);
                    ps.execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.lock.readLock().unlock();
            }
        }, this.ventis.getExecutor());
    }

    /**
     * This configs current connection
     *
     * @return the {@link SqlConfig}'s provided connection
     * @throws SQLException when the database isn't active
     */
    public java.sql.Connection getConnection() throws SQLException {
        return this.sqlConfig.getConnection().getConnection();
    }

    /**
     * Checks if this lock is locked
     *
     * @return if this is not connected
     */
    public boolean checkLock() {
        this.lock.readLock().lock();

        if (!this.connected) {
            this.lock.readLock().unlock();
            return true;
        }

        return false;
    }

    /**
     * Cleans up this sql instance.
     *
     * @see Connection#close()
     */
    @Override
    public void close() {
        this.cleanupTask.close();
        this.messagesTask.close();

        if (this.sqlConfig.getConnection() != null) {
            this.sqlConfig.getConnection().close();
        }

        super.close();
    }
}