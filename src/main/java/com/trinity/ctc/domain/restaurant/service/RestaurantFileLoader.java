package com.trinity.ctc.domain.restaurant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.category.repository.CategoryRepository;
import com.trinity.ctc.domain.restaurant.entity.Menu;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.entity.RestaurantCategory;
import com.trinity.ctc.domain.restaurant.entity.RestaurantImage;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.JsonParseErrorCode;
import com.trinity.ctc.util.formatter.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantFileLoader {

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public List<Restaurant> loadRestaurantsFromFile(List<Category> categories) {
        List<Restaurant> restaurants = new ArrayList<>();

        for (int i = 1; i <= categories.size(); i++) {
            String restaurantPath = "crawlingData/restaurant/restaurants_table" + i + ".json";
            String menuPath = "crawlingData/menu/menus_table" + i + ".json";

            try {
                JsonNode rootNode = readJsonFile(restaurantPath);
                JsonNode menusNode = readJsonFile(menuPath);

                for (JsonNode restaurantNode : rootNode) {
                    List<RestaurantImage> images = parseImages(restaurantNode);
                    List<Menu> menuList = parseMenus(menusNode, restaurantNode);

                    Restaurant restaurant = parseRestaurant(restaurantNode,menuList);
                    restaurant.addImageList(images);
                    restaurant.addMenuList(menuList);

                    attachCategory(restaurant, categories, restaurantNode);

                    restaurants.add(restaurantRepository.save(restaurant));
                }
            } catch (IOException e) {
                throw new CustomException(JsonParseErrorCode.FILE_READ_ERROR);
            }
        }
        return restaurants;
    }

    private JsonNode readJsonFile(String filePath) throws IOException {
        InputStream inputStream = new ClassPathResource(filePath).getInputStream();
        return objectMapper.readTree(inputStream);
    }


    private Restaurant parseRestaurant(JsonNode restaurantNode, List<Menu> menuList) {
        int sum = 0;
        for (Menu menu : menuList) {
            sum += menu.getPrice();
        }
        int length = menuList.size();

        String name = restaurantNode.get("name").asText();
        String address = restaurantNode.get("address").asText();
        String phoneNumber = parsePhoneNumber(restaurantNode.get("phone_number"));
        String operatingHour = restaurantNode.get("operating_hour").asText();
        String expandedDays = restaurantNode.get("expanded_days").asText();
        String timeRange = restaurantNode.get("time_range").asText();
        String convenience = restaurantNode.get("convenience").asText();
        String caution = restaurantNode.get("caution").asText();
        boolean isDeleted = restaurantNode.get("is_deleted").asBoolean();

        int reviewCount = parseIntValue(restaurantNode.get("review"), 0);
        double rating = parseDoubleValue(restaurantNode.get("score"), 0.0);

        return Restaurant.builder()
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
            .averagePrice(length>0 ? sum/menuList.size() : 0)
            .build();
    }

    private List<RestaurantImage> parseImages(JsonNode restaurantNode) {
        List<RestaurantImage> images = new ArrayList<>();
        String imageUrls = restaurantNode.get("image_urls").asText();

        for (String url : imageUrls.split(", ")) {
            if (!url.equals("이미지 정보 없음")) {
                url = url.substring(2);
            }
            images.add(RestaurantImage.builder().url(url).build());
        }
        return images;
    }

    private List<Menu> parseMenus(JsonNode menusNode, JsonNode restaurantNode) {
        List<Menu> menuList = new ArrayList<>();
        String restaurantJsonId = restaurantNode.get("id").asText();

        for (JsonNode menuNode : menusNode) {
            if (menuNode.get("restaurant_id").asText().equals(restaurantJsonId)) {
                String menuName = menuNode.get("menu_name").asText();
                int price = menuNode.get("price").asInt();
                boolean isDeleted = menuNode.get("is_deleted").asBoolean();
                boolean isActive = menuNode.get("is_active").asBoolean();

                menuList.add(Menu.builder()
                    .name(menuName)
                    .price(price)
                    .isDeleted(isDeleted)
                    .isActive(isActive)
                    .build());
            }
        }
        return menuList;
    }

    private void attachCategory(Restaurant restaurant, List<Category> categories, JsonNode restaurantNode) {
        Long categoryId = parseCategoryId(restaurantNode);
        Category category = categories.stream()
            .filter(c -> c.getId()==categoryId)
            .findFirst()
            .orElse(null);

        if (category != null) {
            restaurant = restaurantRepository.save(restaurant);
            category = categoryRepository.save(category);
            restaurant.addCategory(RestaurantCategory.builder()
                .restaurant(restaurant)
                .category(category)
                .build());
        }
    }

    private String parsePhoneNumber(JsonNode phoneNode) {
        if (phoneNode == null || phoneNode.isNull()) {
            return null;
        }

        String phoneNumberStr;

        if (phoneNode.isNumber()) {
            // 숫자로 인식되면 long으로 변환 후 문자열로 변환 (E 표기법 방지)
            phoneNumberStr = String.valueOf(phoneNode.asLong());
        } else {
            // 이미 문자열이면 그대로 사용
            phoneNumberStr = phoneNode.asText();
        }

        // 0으로 시작하지 않는 경우 '0' 추가
        if (!phoneNumberStr.startsWith("0")) {
            phoneNumberStr = "0" + phoneNumberStr;
        }

        return PhoneNumberUtil.formatPhoneNumber(phoneNumberStr);
    }

    private int parseIntValue(JsonNode node, int defaultValue) {
        try {
            return node.asInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double parseDoubleValue(JsonNode node, double defaultValue) {
        try {
            return node.asDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Long parseCategoryId(JsonNode restaurantNode) {
        String restaurantIdStr = restaurantNode.get("id").asText();
        return Long.parseLong(restaurantIdStr.substring(0, 2)) + 1;
    }

}