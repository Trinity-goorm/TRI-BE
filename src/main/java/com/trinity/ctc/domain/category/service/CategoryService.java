package com.trinity.ctc.domain.category.service;

import com.trinity.ctc.domain.category.repository.CategoryBatchInsert;
import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.CategoryErrorCode;
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
            throw new CustomException(CategoryErrorCode.EMPTY_CATEGORIES);
        }
    }
}
