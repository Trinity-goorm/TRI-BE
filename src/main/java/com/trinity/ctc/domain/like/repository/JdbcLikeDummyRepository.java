package com.trinity.ctc.domain.like.repository;

import com.trinity.ctc.domain.like.entity.Likes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcLikeDummyRepository implements LikeDummyRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void batchInsertLikes(List<Likes> likes, int batchSize) {
        String sql = "INSERT INTO likes (user_id, restaurant_id) VALUES (?, ?)";

        log.info("✅ Likes Insert 시작");

        for (Likes like : likes) {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, like.getUser().getId());
                ps.setLong(2, like.getRestaurant().getId());

                return ps;
            });
        }

        log.info("✅ Likes Insert 완료");
    }
}