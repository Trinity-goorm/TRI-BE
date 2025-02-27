package com.trinity.ctc.domain.category.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.JsonParseErrorCode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class CategoryFileLoader {

    public List<Category> loadCategoriesFromFile() {
        List<Category> categories = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream= new ClassPathResource("crawlingData/categories.json").getInputStream();
            JsonNode rootNode = objectMapper.readTree(inputStream);

            for (JsonNode node : rootNode) {
                Category category
                    = Category.builder()
                        .name(node.get("name").asText())
                        .isDeleted(node.get("is_deleted").asBoolean()).build();
                categories.add(category);
            }
        } catch (IOException e) {
            throw new CustomException(JsonParseErrorCode.FILE_READ_ERROR);
        }
        return categories;
    }
}
