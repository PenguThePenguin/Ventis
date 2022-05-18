package me.pengu.ventis.connection.implementation.sql.tasks;

import me.pengu.ventis.connection.implementation.sql.SqlConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Cleans up Sql packets that are more than a minute old
 * Implements {@link Runnable} for a task to check for updates.
 */
public class SqlCleanupTask implements Runnable {

    private final SqlConnection connection;
    private final ScheduledFuture<?> task;

    /**
     * Sql Cleanup instance.
     *
     * @param connection {@link SqlConnection} instance
     */
    public SqlCleanupTask(SqlConnection connection) {
        this.connection = connection;
        this.task = this.connection.getVentis().getExecutor().scheduleAtFixedRate(
                this, 0L, 30L, TimeUnit.SECONDS
        );
    }

    /**
     * An implementation of {@link Runnable#run()}.
     */
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

    /**
     * Cleans up this task.
     */
    public void close() {
        if (!this.task.isCancelled()) this.task.cancel(true);
    }
}