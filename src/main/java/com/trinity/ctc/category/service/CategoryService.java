package com.trinity.ctc.category.service;

import com.trinity.ctc.category.repository.CategoryBatchInsert;
import com.trinity.ctc.domain.category.entity.Category;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryFileLoader categoryFileLoader;
    private final CategoryBatchInsert categoryBatchInsert;

    public CategoryService(CategoryFileLoader categoryFileLoader, CategoryBatchInsert categoryBatchInsert) {
        this.categoryFileLoader = categoryFileLoader;
        this.categoryBatchInsert = categoryBatchInsert;
    }

    public void insertCategoriesFromFile() {
        List<Category> categories = categoryFileLoader.loadCategoriesFromFile();
        if (!categories.isEmpty()) {
            categoryBatchInsert.batchInsertCategories(categories);
        } else {
            System.out.println("No categories found in file.");
        }
    }
}
