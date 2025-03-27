package com.trinity.ctc.domain.fcm.repository;

import com.trinity.ctc.domain.fcm.entity.Fcm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcFcmDummyRepository implements FcmDummyRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void batchInsertFcms(List<Fcm> fcms, int batchSize) {
        String sql = "INSERT INTO fcm (token, registered_at, expires_at, receiver_id) VALUES (?, ?, ?, ?)";

        log.info("✅ Fcm Insert 시작");

        for (Fcm fcm : fcms) {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, fcm.getToken());
                ps.setObject(2, fcm.getRegisteredAt());
                ps.setObject(3, fcm.getExpiresAt());
                ps.setLong(4, fcm.getUser().getId());

                return ps;
            });
        }

        log.info("✅ Fcm Insert 완료");
    }
}
