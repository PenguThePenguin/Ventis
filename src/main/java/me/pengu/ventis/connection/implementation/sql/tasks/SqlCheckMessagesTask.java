package me.pengu.ventis.connection.implementation.sql.tasks;

import me.pengu.ventis.connection.implementation.sql.SqlConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Checks the provided Sql database for any packet updates every second
 * Implements {@link Runnable} for a task to check for data.
 */
public class SqlCheckMessagesTask implements Runnable {

    private final SqlConnection connection;
    private final ScheduledFuture<?> task;

    /**
     * Sql Check Messages instance.
     *
     * @param connection {@link SqlConnection} instance
     */
    public SqlCheckMessagesTask(SqlConnection connection) {
        this.connection = connection;
        this.task = this.connection.getVentis().getExecutor().scheduleAtFixedRate(
                this, 0L, 1L, TimeUnit.SECONDS
        );
    }

    /**
     * An implementation of {@link Runnable#run()}.
     */
    @Override
    public void run() {
        if (this.connection.checkLock()) return;

        try (PreparedStatement ps = this.connection.prepareStatement("SELECT `id`, 'channel', `message`, FROM `" + this.connection.getTableName() + "` WHERE `id` > ? AND (NOW() - `time` < 30)")) {
            ps.setLong(1, this.connection.getLastId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    long id = rs.getLong("id");
                    this.connection.setLastId(Math.max(this.connection.getLastId(), id));

                    String message = rs.getString("channel");
                    String channel = rs.getString("message");

                    this.connection.handleMessage(channel, message);
                }
            }
        } catch (SQLException e) {
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