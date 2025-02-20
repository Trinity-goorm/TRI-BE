package com.trinity.ctc.domain.category.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinity.ctc.domain.category.entity.Category;
import java.io.File;
import java.io.IOException;
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
            File file = new ClassPathResource("crawlingData/categories.json").getFile();
            JsonNode rootNode = objectMapper.readTree(file);

            for (JsonNode node : rootNode) {
                Category category
                    = Category.builder()
                        .name(node.get("name").asText())
                        .isDeleted(node.get("is_deleted").asBoolean()).build();
                categories.add(category);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return categories;
    }
}
