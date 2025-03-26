package com.trinity.ctc.domain.like.service;

import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.like.factory.LikeFactory;
import com.trinity.ctc.domain.like.repository.LikeDummyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeDummyService {
    private final LikeDummyRepository likesDummyRepository;
    private final LikeFactory likesFactory;

    @Transactional
    public void generateDummyData(List<Map<String, String>> likesCsv, int batchSize) {
        log.info("✅ [LikeDummyService] Like CSV 데이터 파싱 및 생성 시작");
        List<Likes> likes = likesFactory.createLikesByCsv(likesCsv);
        log.info("✅ [LikeDummyService] 생성된 Like 개수: {}", likes.size());

        log.info("✅ [LikeDummyService] Like 배치 저장 시작");
        likesDummyRepository.batchInsertLikes(likes, batchSize);
        log.info("✅ [LikeDummyService] Like 배치 저장 완료");
    }
}
