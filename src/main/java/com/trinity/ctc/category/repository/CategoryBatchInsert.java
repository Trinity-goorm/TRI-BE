package com.trinity.ctc.category.repository;

import com.trinity.ctc.domain.category.entity.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CategoryBatchInsert {

    private final JdbcTemplate jdbcTemplate;

    public CategoryBatchInsert(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void batchInsertCategories(List<Category> categories) {
        String sql = "INSERT INTO category (name, is_deleted) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, categories, 100, (ps, category) -> {
            ps.setString(1, category.getName());
            ps.setBoolean(2, category.getIsDeleted());
        });
    }
}

