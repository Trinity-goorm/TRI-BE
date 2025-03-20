package com.trinity.ctc.domain.user.repository;

import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.entity.UserPreference;
import com.trinity.ctc.domain.user.entity.UserPreferenceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcUserDummyRepository implements UserDummyRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void batchInsertUsers(List<User> users, int batchSize) {
        String sql = "INSERT INTO user (kakao_id, nickname, status, phone_number, normal_ticket_count, empty_ticket_count, sex, birthday, image_url, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        log.info("✅ User Insert 시작 - KeyHolder로 PK 추출");

        for (User user : users) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, user.getKakaoId());
                ps.setString(2, user.getNickname());
                ps.setString(3, user.getStatus().name());
                ps.setString(4, user.getPhoneNumber());
                ps.setInt(5, user.getNormalTicketCount());
                ps.setInt(6, user.getEmptyTicketCount());
                ps.setString(7, user.getSex().name());
                ps.setObject(8, user.getBirthday());
                ps.setString(9, user.getImageUrl());
                ps.setBoolean(10, user.getIsDeleted());
                return ps;
            }, keyHolder);

            Long generatedId = keyHolder.getKey().longValue();
            user.setId(generatedId);
            log.debug("✅ 생성된 User ID: {}", generatedId);
        }

        log.info("✅ User Insert 완료");
    }

    @Override
    public void batchInsertUserPreferences(List<UserPreference> userPreferences, int batchSize) {
        // ✅ 수정: user_preference 테이블은 user_id가 PK이므로 컬럼 명확히 지정
        String sql = "INSERT INTO user_preference (user_id, min_price, max_price) VALUES (?, ?, ?)";

        log.info("✅ UserPreference Insert 시작");

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                UserPreference preference = userPreferences.get(i);
                Long userId = preference.getUser().getId();
                if (userId == null) {
                    throw new IllegalStateException("🚨 UserPreference에 매핑된 User ID가 null입니다");
                }
                ps.setLong(1, userId); // @MapsId 연결
                ps.setInt(2, preference.getMinPrice());
                ps.setInt(3, preference.getMaxPrice());
            }

            @Override
            public int getBatchSize() {
                return userPreferences.size();
            }
        });

        log.info("✅ UserPreference Insert 완료");
    }

    @Override
    public void batchInsertUserPreferenceCategories(List<UserPreferenceCategory> userPreferenceCategories, int batchSize) {
        String sql = "INSERT INTO user_preference_category (user_preference_id, category_id) VALUES (?, ?)";

        log.info("✅ UserPreferenceCategory Insert 시작");

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                UserPreferenceCategory upc = userPreferenceCategories.get(i);
                ps.setLong(1, upc.getUserPreference().getId());
                ps.setLong(2, upc.getCategory().getId());
            }

            @Override
            public int getBatchSize() {
                return userPreferenceCategories.size();
            }
        });

        log.info("✅ UserPreferenceCategory Insert 완료");
    }
}
