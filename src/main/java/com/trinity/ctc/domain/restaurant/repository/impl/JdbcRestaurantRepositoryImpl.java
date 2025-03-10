package com.trinity.ctc.domain.restaurant.repository.impl;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.JdbcRestaurantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcRestaurantRepositoryImpl implements JdbcRestaurantRepository {

    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public JdbcRestaurantRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page<Restaurant> searchRestaurants(String keyword, Pageable pageable) {
        String sql = "SELECT DISTINCT r.* FROM restaurant r " +
            "LEFT JOIN menu m ON m.restaurant_id = r.id " +
            "LEFT JOIN restaurant_category rc ON rc.restaurant_id = r.id " +
            "LEFT JOIN category c ON c.id = rc.category_id " +
            "WHERE (LOWER(r.name) LIKE LOWER(?) " +
            "OR LOWER(m.name) LIKE LOWER(?) " +
            "OR LOWER(c.name) LIKE LOWER(?)) " +
            "AND r.average_price > 5000 " +
            "LIMIT ? OFFSET ?";

        String countSql = "SELECT COUNT(DISTINCT r.id) FROM restaurant r " +
            "LEFT JOIN menu m ON m.restaurant_id = r.id " +
            "LEFT JOIN restaurant_category rc ON rc.restaurant_id = r.id " +
            "LEFT JOIN category c ON c.id = rc.category_id " +
            "WHERE (LOWER(r.name) LIKE LOWER(?) " +
            "OR LOWER(m.name) LIKE LOWER(?) " +
            "OR LOWER(c.name) LIKE LOWER(?)) " +
            "AND r.average_price > 5000";

        String wildcardKeyword = "%" + keyword + "%";

        // 메인 쿼리 실행
        List<Restaurant> results = jdbcTemplate.query(
            sql,
            new Object[]{wildcardKeyword, wildcardKeyword, wildcardKeyword,
                pageable.getPageSize(), pageable.getOffset()},
            new RestaurantRowMapper(entityManager)
        );

        // 카운트 쿼리 실행
        Integer total = jdbcTemplate.queryForObject(
            countSql,
            Integer.class,
            wildcardKeyword, wildcardKeyword, wildcardKeyword
        );

        return new PageImpl<>(results, pageable, total != null ? total : 0);
    }

    // 별도의 RowMapper 클래스를 구현하여 JDBC 결과를 엔티티로 변환
    private static class RestaurantRowMapper implements RowMapper<Restaurant> {
        private final EntityManager entityManager;

        public RestaurantRowMapper(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        @Override
        public Restaurant mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");

            // 엔티티 매니저를 통해 엔티티를 가져오거나 새로 생성
            Restaurant restaurant = entityManager.find(Restaurant.class, id);

            if (restaurant == null) {
                restaurant = Restaurant.builder().
                    name(rs.getString("name")).
                    address(rs.getString("address")).
                    phoneNumber(rs.getString("phone_number")).
                    averagePrice(rs.getInt("average_price")).
                    build();
            }

            return restaurant;
        }
    }
}