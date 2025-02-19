package com.trinity.ctc.domain.restaurant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinity.ctc.category.repository.CategoryRepository;
import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.restaurant.entity.Menu;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.entity.RestaurantCategory;
import com.trinity.ctc.domain.restaurant.entity.RestaurantImage;
import com.trinity.ctc.domain.restaurant.repository.RestaurantCategoryRepository;
import com.trinity.ctc.domain.restaurant.repository.RestaurantImageRepository;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantFileLoader {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final CategoryRepository categoryRepository;

    public RestaurantFileLoader(RestaurantRepository restaurantRepository, RestaurantImageRepository restaurantImageRepository, RestaurantCategoryRepository restaurantCategoryRepository,
        CategoryRepository categoryRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantImageRepository = restaurantImageRepository;
        this.restaurantCategoryRepository = restaurantCategoryRepository;
        this.categoryRepository = categoryRepository;
    }
    @Transactional
    public List<Restaurant> loadRestaurantsFromFile(List<Category> categories) {
        List<Restaurant> restaurants = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new ClassPathResource("crawlingData/중식_restaurants_table.json").getFile();
            JsonNode rootNode = objectMapper.readTree(file);
            for (JsonNode restaurantNode : rootNode) {
                // 빈 값이 들어오면 기본값 설정
                String name = restaurantNode.get("name").asText();
                String address = restaurantNode.get("address").asText();
                String phoneNumber = restaurantNode.get("phone_number").decimalValue()
                    .toPlainString();
                if (!phoneNumber.equals("")) {
                    phoneNumber = "0" + phoneNumber.replace(".0", "");
                }
                String operatingHour = restaurantNode.get("operating_hour").asText();
                String expandedDays = restaurantNode.get("expanded_days").asText();
                String timeRange = restaurantNode.get("time_range").asText();
                String convenience = restaurantNode.get("convenience").asText();
                String caution = restaurantNode.get("caution").asText();

                boolean isDeleted = restaurantNode.get("is_deleted").asBoolean();

                phoneNumber = "0"+phoneNumber.substring(1, phoneNumber.length() - 1); // 전화번호 양 끝 따옴표 제거
                // review & score 기본값 처리 (예외 발생 방지)
                int reviewCount = 0;
                double rating = 0.0;

                if (!restaurantNode.get("review").asText().isEmpty()) {
                    try {
                        reviewCount = restaurantNode.get("review").asInt();
                    } catch (Exception e) {
                        reviewCount = 0; // 기본값 설정
                    }
                }

                if (restaurantNode.get("score").asText().isEmpty()) {
                    try {
                        rating = restaurantNode.get("score").asDouble();
                    } catch (Exception e) {
                        rating = 0.0; // 기본값 설정
                    }
                }

                // Restaurant 객체 생성
                Restaurant restaurant = Restaurant.builder()
                    .name(name)
                    .address(address)
                    .phoneNumber(phoneNumber)
                    .convenience(convenience)
                    .operatingHour(operatingHour)
                    .expandedDays(expandedDays)
                    .timeRange(timeRange)
                    .caution(caution)
                    .isDeleted(isDeleted)
                    .reviewCount(reviewCount)
                    .rating(rating)
                    .build();


                //이미지 파싱 및 연결
                List<RestaurantImage> images = new ArrayList<>();
                String imageUrls = restaurantNode.get("image_urls").asText();
                for (String url : imageUrls.split(", ")) {
                    if (!url.equals("이미지 정보 없음")) {
                        url = url.substring(2, url.length());
                    }
                    RestaurantImage image = RestaurantImage.builder()
                        .url(url)
                        .build();

                    images.add(image);
                }
                restaurant.addImageList(images);

                //카테고리 연결
                String restaurantIdStr = restaurantNode.get("id").asText();
                Long restaurantIdPrefix = Long.parseLong(restaurantIdStr.substring(0, 2)); // 앞 2자리
                Long categoryId = restaurantIdPrefix + 1; // +1 연산

                Category category = categories.stream()
                    .filter(c -> c.getId() == categoryId)
                    .findFirst()
                    .orElse(null);

                restaurant = restaurantRepository.save(restaurant);
                category = categoryRepository.save(category);

                RestaurantCategory restaurantCategory = RestaurantCategory.builder()
                    .restaurant(restaurant)
                    .category(category)
                    .build();
                restaurant.addCategory(restaurantCategory);

//                //메뉴 연결
                File menuFile = new ClassPathResource("crawlingData/중식_menus_table.json").getFile();
                JsonNode menusNode = objectMapper.readTree(menuFile);

                List<Menu> menuList = new ArrayList<>();
                for (JsonNode menuNode : menusNode) {
                    String menuJsonId = menuNode.get("restaurant_id").asText();
                    String restaurantJsonId = restaurantNode.get("id").asText();
                    if (menuJsonId.equals(restaurantJsonId)) {
                        String menuName = menuNode.get("menu_name").asText();
                        int price = menuNode.get("price").asInt();
                        boolean isDeletedMenu = menuNode.get("is_deleted").asBoolean();
                        boolean isActiveMenu = menuNode.get("is_active").asBoolean();

                        // 메뉴 객체 생성
                        Menu menu = Menu.builder()
                            .name(menuName)
                            .price(price)
                            .isDeleted(isDeletedMenu)
                            .isActive(isActiveMenu)
                            .build();

                        menuList.add(menu);
                    }
                }
                restaurant.addMenuList(menuList);
                restaurants.add(restaurant);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return restaurants;
    }
}