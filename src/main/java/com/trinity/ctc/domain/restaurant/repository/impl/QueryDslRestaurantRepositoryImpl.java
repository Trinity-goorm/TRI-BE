package com.trinity.ctc.domain.restaurant.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trinity.ctc.domain.category.entity.QCategory;
import com.trinity.ctc.domain.restaurant.entity.QMenu;
import com.trinity.ctc.domain.restaurant.entity.QRestaurant;
import com.trinity.ctc.domain.restaurant.entity.QRestaurantCategory;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.QueryDslRestaurantRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class QueryDslRestaurantRepositoryImpl implements QueryDslRestaurantRepository {

    private final JPAQueryFactory queryFactory;

    public QueryDslRestaurantRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Restaurant> searchRestaurants(String keyword, Pageable pageable) {
        QRestaurant restaurant = QRestaurant.restaurant;
        QMenu menu = QMenu.menu;
        QRestaurantCategory restaurantCategory = QRestaurantCategory.restaurantCategory;
        QCategory category = QCategory.category;

        // 검색 조건
        BooleanExpression predicate = restaurant.averagePrice.gt(5000)
            .and(restaurant.name.toLowerCase().like("%" + keyword.toLowerCase() + "%")
                .or(menu.name.toLowerCase().like("%" + keyword.toLowerCase() + "%"))
                .or(category.name.toLowerCase().like("%" + keyword.toLowerCase() + "%")));

        // 결과 쿼리
        List<Restaurant> results = queryFactory
            .selectDistinct(restaurant)
            .from(restaurant)
            .leftJoin(restaurant.menus, menu)
            .leftJoin(restaurant.restaurantCategoryList, restaurantCategory)
            .leftJoin(restaurantCategory.category, category)
            .where(predicate)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
            .select(restaurant.countDistinct())
            .from(restaurant)
            .leftJoin(restaurant.menus, menu)
            .leftJoin(restaurant.restaurantCategoryList, restaurantCategory)
            .leftJoin(restaurantCategory.category, category)
            .where(predicate);

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }
}
