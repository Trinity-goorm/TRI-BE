package com.trinity.ctc.domain.notification.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JdbcNotificationHistoryRepository implements NotificationHistoryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void batchInsertNotificationHistories(List<NotificationHistory> notificationHistories) {
        String sql = """
                    INSERT INTO notification_history 
                    (type, message, sent_at, sent_result, error_code, fcm_token, is_deleted, receiver_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        List<List<NotificationHistory>> batches = Lists.partition(notificationHistories, 1000);

        for (List<NotificationHistory> batch : batches) {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    NotificationHistory notificationHistory = batch.get(i);
                    ps.setString(1, notificationHistory.getType().name());
                    ps.setString(2, convertMapToJson(notificationHistory.getMessage()));
                    ps.setObject(3, notificationHistory.getSentAt());
                    ps.setString(4, notificationHistory.getSentResult().name());
                    ps.setString(5, notificationHistory.getErrorCode() == null ? null : notificationHistory.getErrorCode().name());
                    ps.setString(6, notificationHistory.getFcmToken());
                    ps.setBoolean(7, false);
                    ps.setLong(8, notificationHistory.getUser().getId());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    private String convertMapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) return null;

        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}
