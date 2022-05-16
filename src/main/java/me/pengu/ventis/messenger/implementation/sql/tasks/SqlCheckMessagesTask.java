package me.pengu.ventis.messenger.implementation.sql.tasks;

import me.pengu.ventis.messenger.implementation.sql.SqlMessenger;
import me.pengu.ventis.packet.handler.PacketHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Checks the provided Sql database for any packet updates
 * - every second
 */
public class SqlCheckMessagesTask implements Runnable {

    private final SqlMessenger messenger;
    private final ReadWriteLock lock;

    private final ScheduledFuture<?> task;

    public SqlCheckMessagesTask(SqlMessenger messenger) {
        this.messenger = messenger;
        this.lock = this.messenger.getLock();

        this.task = this.messenger.getVentis().getExecutor().scheduleAtFixedRate(
                this, 0L, 1L, TimeUnit.SECONDS
        );
    }

    @Override
    public void run() {
        this.lock.readLock().lock();

        if (!this.messenger.isConnected()) {
            this.lock.readLock().unlock();
            return;
        }

        try (Connection connection = this.messenger.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT `id`, 'channel', `message`, FROM `" + this.messenger.getTableName() + "` WHERE `id` > ? AND (NOW() - `time` < 30)")) {
                ps.setLong(1, this.messenger.getLastId());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {

                        long id = rs.getLong("id");
                        this.messenger.setLastId(Math.max(this.messenger.getLastId(), id));

                        String message = rs.getString("message");
                        String channel = rs.getString("channel");

                        this.messenger.handleMessage(channel, message);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void close() {
        if (this.task != null) this.task.cancel(true);
    }
}
