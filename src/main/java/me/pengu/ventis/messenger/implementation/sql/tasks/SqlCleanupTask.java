package me.pengu.ventis.messenger.implementation.sql.tasks;

import me.pengu.ventis.messenger.implementation.sql.SqlMessenger;
import me.pengu.ventis.packet.handler.PacketHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Cleans up Sql packets that are more than a minute old
 */
public class SqlCleanupTask implements Runnable {

    private final SqlMessenger messenger;
    private final ReadWriteLock lock;

    private final ScheduledFuture<?> task;

    public SqlCleanupTask(SqlMessenger messenger) {
        this.messenger = messenger;
        this.lock = messenger.getLock();

        this.task = this.messenger.getVentis().getExecutor().scheduleAtFixedRate(
                this, 0L, 30L, TimeUnit.SECONDS
        );
    }

    @Override
    public void run() {
        this.lock.readLock().lock();

        if (!this.messenger.isConnected()) {
            lock.readLock().unlock();
            return;
        }

        try (Connection connection = this.messenger.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM `" + this.messenger.getTableName() + "` WHERE (NOW() - `time` > 60)")) {
                ps.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void close() {
        if (this.task != null) this.task.cancel(true);
    }
}
