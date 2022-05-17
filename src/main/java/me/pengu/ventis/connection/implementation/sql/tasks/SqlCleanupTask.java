package me.pengu.ventis.connection.implementation.sql.tasks;

import me.pengu.ventis.connection.implementation.sql.SqlConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Cleans up Sql packets that are more than a minute old
 */
public class SqlCleanupTask implements Runnable {

    private final SqlConnection connection;
    private final ScheduledFuture<?> task;

    public SqlCleanupTask(SqlConnection connection) {
        this.connection = connection;
        this.task = this.connection.getVentis().getExecutor().scheduleAtFixedRate(
                this, 0L, 30L, TimeUnit.SECONDS
        );
    }

    @Override
    public void run() {
        if (this.connection.checkLock()) return;

        try (Connection connection = this.connection.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM `" + this.connection.getTableName() + "` WHERE (NOW() - `time` > 60)")) {
                ps.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.connection.getLock().readLock().unlock();
        }
    }

    public void close() {
        if (!this.task.isCancelled()) this.task.cancel(true);
    }
}