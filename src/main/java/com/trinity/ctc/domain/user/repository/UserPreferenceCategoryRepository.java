package com.trinity.ctc.domain.user.repository;

import com.trinity.ctc.domain.user.entity.UserPreferenceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferenceCategoryRepository extends JpaRepository<UserPreferenceCategory, Long> {

}

