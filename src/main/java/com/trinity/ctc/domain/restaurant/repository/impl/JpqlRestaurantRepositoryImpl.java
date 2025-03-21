package com.trinity.ctc.domain.restaurant.repository.impl;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.JpqlRestaurantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpqlRestaurantRepositoryImpl implements JpqlRestaurantRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Restaurant> searchRestaurants(String keyword, Pageable pageable) {
        // JPQL 쿼리 정의
        String jpql = "SELECT DISTINCT r FROM Restaurant r " +
            "LEFT JOIN r.menus m " +
            "LEFT JOIN r.restaurantCategoryList rc " +
            "LEFT JOIN rc.category c " +
            "WHERE (LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND r.averagePrice > 5000";

        // 카운트 쿼리
        String countJpql = "SELECT COUNT(DISTINCT r) FROM Restaurant r " +
            "LEFT JOIN r.menus m " +
            "LEFT JOIN r.restaurantCategoryList rc " +
            "LEFT JOIN rc.category c " +
            "WHERE (LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND r.averagePrice > 5000";

        // 메인 쿼리 실행
        TypedQuery<Restaurant> query = entityManager.createQuery(jpql, Restaurant.class)
            .setParameter("keyword", keyword)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize());

        // 정렬 조건 적용 (실제 구현에서는 페이징 정보에서 정렬 조건을 추출하여 적용)
        List<Restaurant> results = query.getResultList();

        // 카운트 쿼리 실행
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class)
            .setParameter("keyword", keyword);
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }
}
