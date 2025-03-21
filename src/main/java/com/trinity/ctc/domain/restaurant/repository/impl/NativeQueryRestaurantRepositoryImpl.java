package com.trinity.ctc.domain.restaurant.repository.impl;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.NativeQueryRestaurantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NativeQueryRestaurantRepositoryImpl implements NativeQueryRestaurantRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Restaurant> searchRestaurants(String keyword, Pageable pageable) {
        // Native SQL 쿼리
        String sql = "SELECT DISTINCT r.* FROM restaurant r " +
            "LEFT JOIN menu m ON m.restaurant_id = r.id " +
            "LEFT JOIN restaurant_category rc ON rc.restaurant_id = r.id " +
            "LEFT JOIN category c ON c.id = rc.category_id " +
            "WHERE (LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND r.average_price > 5000";

        // 카운트 쿼리
        String countSql = "SELECT COUNT(DISTINCT r.id) FROM restaurant r " +
            "LEFT JOIN menu m ON m.restaurant_id = r.id " +
            "LEFT JOIN restaurant_category rc ON rc.restaurant_id = r.id " +
            "LEFT JOIN category c ON c.id = rc.category_id " +
            "WHERE (LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND r.average_price > 5000";

        // 메인 쿼리 실행
        Query query = entityManager.createNativeQuery(sql, Restaurant.class)
            .setParameter("keyword", keyword)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Restaurant> results = query.getResultList();

        // 카운트 쿼리 실행
        Query countQuery = entityManager.createNativeQuery(countSql)
            .setParameter("keyword", keyword);
        Number total = (Number) countQuery.getSingleResult();

        return new PageImpl<>(results, pageable, total.longValue());
    }
}
