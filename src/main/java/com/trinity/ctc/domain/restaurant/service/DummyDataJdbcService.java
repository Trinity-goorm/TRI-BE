package com.trinity.ctc.domain.restaurant.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DummyDataJdbcService {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    private static final String INSERT_RESTAURANT_SQL = """
            INSERT INTO restaurant (name, address, phone_number, convenience, operating_hour, expanded_days, 
            time_range, caution, is_deleted, review_count, rating, average_price) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String INSERT_MENU_SQL = """
            INSERT INTO menu (name, price, image_url, is_active, is_deleted, restaurant_id) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final int BATCH_SIZE = 5000; // ✅ 배치 크기 조절 (한 번에 5000개씩 삽입)
    private List<Long> dummyRestaurantIds = new ArrayList<>();
    @Transactional
    public void insertRestaurantDummyData() {
        // ✅ 기존 restaurant 가져오기
        List<RestaurantData> originalRestaurants = jdbcTemplate.query("SELECT * FROM restaurant",
            (rs, rowNum) -> new RestaurantData(
                rs.getString("name"),
                rs.getString("address"),
                rs.getString("phone_number"),
                rs.getString("convenience"),
                rs.getString("operating_hour"),
                rs.getString("expanded_days"),
                rs.getString("time_range"),
                rs.getString("caution"),
                rs.getBoolean("is_deleted"),
                rs.getInt("review_count"),
                rs.getDouble("rating"),
                rs.getInt("average_price")
            ));
        log.info("기존 레스토랑 개수: {}", originalRestaurants.size());

        // ✅ 더미 레스토랑 데이터 삽입 및 ID 추출


        List<Object[]> batchParams = new ArrayList<>();
        for (RestaurantData restaurant : originalRestaurants) {
            for (int i = 0; i < 1000; i++) {
                batchParams.add(new Object[]{
                    addRandomString(restaurant.name),
                    restaurant.address,
                    restaurant.phoneNumber,
                    restaurant.convenience,
                    restaurant.operatingHour,
                    restaurant.expandedDays,
                    restaurant.timeRange,
                    restaurant.caution,
                    false,
                    restaurant.reviewCount,
                    restaurant.rating,
                    restaurant.averagePrice
                });

                if (batchParams.size() >= BATCH_SIZE) {
                    jdbcTemplate.batchUpdate(INSERT_RESTAURANT_SQL, batchParams);
                    batchParams.clear();
                    log.info("레스토랑 데이터 {}개 삽입 완료", BATCH_SIZE);
                }
            }
        }
        if (!batchParams.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_RESTAURANT_SQL, batchParams);
            log.info("레스토랑 데이터 최종 삽입 완료 (남은 개수: {})", batchParams.size());
            batchParams.clear();
        }

    }

    @Transactional
    public void insertMenuDummyData() {
        // ✅ 삽입된 레스토랑 ID 가져오기
        dummyRestaurantIds = jdbcTemplate.queryForList(
            "SELECT id FROM restaurant ORDER BY id",
            Long.class
        );
        log.info("더미 레스토랑 ID 개수: {}", dummyRestaurantIds.size());
        // ✅ 기존 메뉴 가져오기
        List<MenuData> originalMenus = jdbcTemplate.query("SELECT * FROM menu",
            (rs, rowNum) -> new MenuData(
                rs.getString("name"),
                rs.getInt("price"),
                rs.getString("image_url"),
                rs.getBoolean("is_active"),
                rs.getBoolean("is_deleted")
            ));
        log.info("기존 메뉴 개수: {}", originalMenus.size());
        // ✅ 더미 메뉴 데이터 삽입

        int i = 0;
        List<Object[]> menuBatchParams = new ArrayList<>();
        for (int j = 2256; j < dummyRestaurantIds.size(); j++) {
            long restaurantId = dummyRestaurantIds.get(j);
            for (int k = 0; k < 5; k++) {
                MenuData menu = originalMenus.get(i);
                    menuBatchParams.add(new Object[]{
                        addRandomString(menu.name),
                        menu.price,
                        menu.imageUrl,
                        menu.isActive,
                        false,
                        restaurantId
                    });

                    if (menuBatchParams.size() >= BATCH_SIZE) {
                        jdbcTemplate.batchUpdate(INSERT_MENU_SQL, menuBatchParams);
                        menuBatchParams.clear();
                        log.info("메뉴 데이터 {}개 삽입 완료", BATCH_SIZE);
                        log.info("남은 식당 수 {}", dummyRestaurantIds.size() - j);
                    }
            }
            i = (i + 1) % originalMenus.size();
        }
        if (!menuBatchParams.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_MENU_SQL, menuBatchParams);
            log.info("메뉴 데이터 최종 삽입 완료 (남은 개수: {})", menuBatchParams.size());
            menuBatchParams.clear();
        }
    }

    private String addRandomString(String baseName) {
        String randomStr = UUID.randomUUID().toString().substring(0, 5);
        return random.nextBoolean() ? randomStr + "_" + baseName : baseName + "_" + randomStr;
    }

    private static class RestaurantData {
        String name, address, phoneNumber, convenience, operatingHour, expandedDays, timeRange, caution;
        boolean isDeleted;
        int reviewCount, averagePrice;
        double rating;

        public RestaurantData(String name, String address, String phoneNumber, String convenience, String operatingHour,
            String expandedDays, String timeRange, String caution, boolean isDeleted, int reviewCount,
            double rating, int averagePrice) {
            this.name = name;
            this.address = address;
            this.phoneNumber = phoneNumber;
            this.convenience = convenience;
            this.operatingHour = operatingHour;
            this.expandedDays = expandedDays;
            this.timeRange = timeRange;
            this.caution = caution;
            this.isDeleted = isDeleted;
            this.reviewCount = reviewCount;
            this.rating = rating;
            this.averagePrice = averagePrice;
        }
    }

    private static class MenuData {
        String name, imageUrl;
        int price;
        boolean isActive, isDeleted;

        public MenuData(String name, int price, String imageUrl, boolean isActive, boolean isDeleted) {
            this.name = name;
            this.price = price;
            this.imageUrl = imageUrl;
            this.isActive = isActive;
            this.isDeleted = isDeleted;
        }
    }
}