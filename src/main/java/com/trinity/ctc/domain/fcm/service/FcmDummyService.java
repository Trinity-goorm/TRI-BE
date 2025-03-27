package com.trinity.ctc.domain.fcm.service;

import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.fcm.factory.FcmFactory;
import com.trinity.ctc.domain.fcm.repository.FcmDummyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmDummyService {
    private final FcmDummyRepository fcmDummyRepository;
    private final FcmFactory fcmFactory;

    @Transactional
    public void generateDummyData(List<Map<String, String>> fcmCsv, int batchSize) {
        log.info("✅ [FcmDummyService] Fcm CSV 데이터 파싱 및 생성 시작");
        List<Fcm> fcmList = fcmFactory.createFcmListByCsv(fcmCsv);
        log.info("✅ [FcmDummyService] 생성된 Fcm 개수: {}", fcmList.size());

        log.info("✅ [FcmDummyService] Fcm 배치 저장 시작");
        fcmDummyRepository.batchInsertFcms(fcmList, batchSize);
        log.info("✅ [FcmDummyService] Fcm 배치 저장 완료");
    }
}