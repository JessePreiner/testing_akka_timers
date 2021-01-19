package com.jessepreiner.scheduling.readside;

import akka.projection.jdbc.JdbcSession;

public class MySqlScheduleProjectionDao {

    static class ScheduleProjectionRecord {
        private final String scheduleId;
        private final String status;

        ScheduleProjectionRecord(String scheduleId, String status) {
            this.scheduleId = scheduleId;
            this.status = status;
        }
    }
    void save(ScheduleProjectionRecord scheduleProjectionRecord, JdbcSession session) {
        try {

            String sql = String.format("INSERT INTO `akka`.ScheduleProjection (scheduleId, status, numUpdates) values ('%s', '%s', coalesce(numUpdates,0) + 1)" +
            " ON DUPLICATE KEY UPDATE Status = '%s', numUpdates = coalesce(numUpdates,0) + 1;", scheduleProjectionRecord.scheduleId,  scheduleProjectionRecord.status, scheduleProjectionRecord.status);

            session.withConnection(connection -> {
                int rowsAffected = connection.createStatement().executeUpdate(sql);
                return rowsAffected > 0;
            });

        } catch (Exception e) {
           System.out.println(e.getMessage());
        }

    }
}
