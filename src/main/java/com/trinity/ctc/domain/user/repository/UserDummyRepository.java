package com.trinity.ctc.domain.user.repository;

import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.entity.UserPreference;
import com.trinity.ctc.domain.user.entity.UserPreferenceCategory;

import java.util.List;

public interface UserDummyRepository {
    void batchInsertUsers(List<User> users, int batchSize);
    void batchInsertUserPreferences(List<UserPreference> userPreferences, int batchSize);
    void batchInsertUserPreferenceCategories(List<UserPreferenceCategory> userPreferenceCategories, int batchSize);
}
