package me.pengu.ventis.messenger.implementation.sql.tasks;

import me.pengu.ventis.messenger.implementation.sql.SqlMessenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Cleans up Sql packets that are more than a minute old
 */
public class SqlCleanupTask implements Runnable {

    private final SqlMessenger messenger;
    private final ScheduledFuture<?> task;

    public SqlCleanupTask(SqlMessenger messenger) {
        this.messenger = messenger;
        this.task = this.messenger.getVentis().getExecutor().scheduleAtFixedRate(
                this, 0L, 30L, TimeUnit.SECONDS
        );
    }

    @Override
    public void run() {
        if (this.messenger.checkLock()) return;

        try (Connection connection = this.messenger.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM `" + this.messenger.getTableName() + "` WHERE (NOW() - `time` > 60)")) {
                ps.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.messenger.getLock().readLock().unlock();
        }
    }

    public void close() {
        if (!this.task.isCancelled()) this.task.cancel(true);
    }
}