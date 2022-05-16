package me.pengu.ventis.messenger.implementation.sql;

import lombok.Getter;
import lombok.Setter;
import me.pengu.ventis.Ventis;
import me.pengu.ventis.messenger.Messenger;
import me.pengu.ventis.messenger.config.SqlConfig;
import me.pengu.ventis.messenger.implementation.sql.tasks.SqlCheckMessagesTask;
import me.pengu.ventis.messenger.implementation.sql.tasks.SqlCleanupTask;
import me.pengu.ventis.packet.Packet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Sql Messenger
 * Extends {@link Messenger} for packet handling.
 */
@Getter @Setter
public class SqlMessenger extends Messenger {

    private final SqlConfig sqlConfig;
    private final String tableName;

    private final SqlCleanupTask cleanupTask;
    private final SqlCheckMessagesTask messagesTask;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private long lastId = 0;

    /**
     * Sql Messenger instance.
     * @param ventis {@link Ventis} instance
     */
    public SqlMessenger(Ventis ventis) {
        super(ventis);

        this.sqlConfig = this.ventis.getConfig().getSqlConfig();
        this.tableName = Messenger.CHANNEL_PREFIX + this.sqlConfig.getChannel();

        this.sqlConfig.getConnection().load(this.sqlConfig);

        this.cleanupTask = new SqlCleanupTask(this);
        this.messagesTask = new SqlCheckMessagesTask(this);
    }

    /**
     * Sends a packet.
     * @param packet packet to send
     * @param channel redis channel to use
     *
     * @return a future to manipulate the result of the operation
     */
    @Override
    public CompletableFuture<Void> sendPacket(Packet packet, String channel) {
        return CompletableFuture.runAsync(() -> {
            this.lock.readLock().lock();

            if (!this.connected) {
                this.lock.readLock().unlock();
                return;
            }

            try (Connection connection = getConnection()) {
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
     * Returns this connection
     * @return the {@link SqlConfig}'s provided connection;
     */
    public Connection getConnection() throws SQLException {
        return this.sqlConfig.getConnection().getConnection();
    }

    /**
     * Cleans up this sql instance.
     * @see Messenger#close()
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
