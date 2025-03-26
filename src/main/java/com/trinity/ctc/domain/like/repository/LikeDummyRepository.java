package com.trinity.ctc.domain.like.repository;

import com.trinity.ctc.domain.like.entity.Likes;

import java.util.List;

public interface LikeDummyRepository {
    void batchInsertLikes(List<Likes> likes, int batchSize);
}
