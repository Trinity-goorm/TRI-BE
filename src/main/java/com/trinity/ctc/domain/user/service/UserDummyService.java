package com.trinity.ctc.domain.user.service;

import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.entity.UserPreference;
import com.trinity.ctc.domain.user.entity.UserPreferenceCategory;
import com.trinity.ctc.domain.user.factory.UserFactory;
import com.trinity.ctc.domain.user.repository.UserDummyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDummyService {
    private final UserDummyRepository userDummyRepository;
    private final UserFactory userFactory;

    @Transactional
    public void generateDummyData(List<Map<String, String>> userCsv,
                                  List<Map<String, String>> preferenceCsv,
                                  List<Map<String, String>> preferenceCategoryCsv,
                                  int batchSize) {

        log.info("✅ [UserDummyService] User CSV 데이터 파싱 및 생성 시작");
        List<User> users = userFactory.createUsersByCsv(userCsv);
        log.info("✅ [UserDummyService] 생성된 User 개수: {}", users.size());

        log.info("✅ [UserDummyService] User 배치 저장 시작");
        userDummyRepository.batchInsertUsers(users, batchSize);
        log.info("✅ [UserDummyService] User 배치 저장 완료");

        Map<String, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getNickname(), user);
        }

        log.info("✅ [UserDummyService] UserPreference CSV 데이터 파싱 및 생성 시작");
        List<UserPreference> preferences = userFactory.createPreferencesByCsv(preferenceCsv, userMap);
        log.info("✅ [UserDummyService] 생성된 UserPreference 개수: {}", preferences.size());

        log.info("✅ [UserDummyService] UserPreference 배치 저장 시작");
        userDummyRepository.batchInsertUserPreferences(preferences, batchSize);
        log.info("✅ [UserDummyService] UserPreference 배치 저장 완료");

        Map<Long, UserPreference> preferenceMap = new HashMap<>();
        for (UserPreference pref : preferences) {
            preferenceMap.put(pref.getId(), pref);
        }

        log.info("✅ [UserDummyService] UserPreferenceCategory CSV 데이터 파싱 및 생성 시작");
        List<UserPreferenceCategory> prefCategories = userFactory.createPreferenceCategoriesByCsv(preferenceCategoryCsv, preferenceMap);
        log.info("✅ [UserDummyService] 생성된 UserPreferenceCategory 개수: {}", prefCategories.size());

        log.info("✅ [UserDummyService] UserPreferenceCategory 배치 저장 시작");
        userDummyRepository.batchInsertUserPreferenceCategories(prefCategories, batchSize);
        log.info("✅ [UserDummyService] UserPreferenceCategory 배치 저장 완료");

        log.info("🎉 [UserDummyService] 더미 데이터 전체 생성 및 저장 완료!");
    }
}
