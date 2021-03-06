package me.pengu.ventis.connection.implementation.sql;

import lombok.Getter;
import lombok.Setter;
import me.pengu.ventis.Ventis;
import me.pengu.ventis.connection.Connection;
import me.pengu.ventis.connection.config.SqlConfig;
import me.pengu.ventis.connection.implementation.sql.tasks.SqlCheckPacketsTask;
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
    private final SqlCheckPacketsTask packetsTask;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private long lastId = 0;

    /**
     * Sql Connection instance.
     *
     * @param ventis    {@link Ventis} instance
     * @param sqlConfig the provided options for this connection
     */
    public SqlConnection(Ventis ventis, SqlConfig sqlConfig) {
        super(ventis, "sql");

        this.sqlConfig = sqlConfig;
        this.tableName = Connection.CHANNEL_PREFIX + this.ventis.getConfig().getChannel();

        this.sqlConfig.getDatabase().load(this.sqlConfig);

        this.cleanupTask = new SqlCleanupTask(this);
        this.packetsTask = new SqlCheckPacketsTask(this);
    }

    /**
     * Sends a packet.
     *
     * @param packet  packet to send
     * @param channel sql channel to use
     * @return a future to manipulate the result of the operation
     */
    @Override
    public CompletableFuture<Void> sendPacket(Packet packet, String channel) {
        return CompletableFuture.runAsync(() -> {
            if (this.checkLock()) return;

            try (PreparedStatement ps = this.prepareStatement("INSERT INTO `" + this.tableName + "` (`time`, 'channel' `packet`) VALUES(NOW(), ?, ?)")) {
                ps.setString(1, CHANNEL_PREFIX + channel);
                ps.setString(2, packet.toString(channel, this.config.getCodec()));
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.lock.readLock().unlock();
            }

        }, this.ventis.getExecutor());
    }

    /**
     * Prepares a statement from provided query
     *
     * @param query The query to be executed
     * @return the prepared statement
     * @throws SQLException when the database isn't active
     */
    public PreparedStatement prepareStatement(String query) throws SQLException {
        return this.getConnection().prepareStatement(query);
    }

    /**
     * This configs current connection
     *
     * @return the {@link SqlConfig}'s provided connection
     * @throws SQLException when the database isn't active
     */
    public java.sql.Connection getConnection() throws SQLException {
        return this.sqlConfig.getDatabase().getConnection();
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
        this.packetsTask.close();

        this.sqlConfig.getDatabase().close();
        super.close();
    }
}