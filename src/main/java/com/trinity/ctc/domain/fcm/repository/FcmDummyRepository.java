package com.trinity.ctc.domain.fcm.repository;

import com.trinity.ctc.domain.fcm.entity.Fcm;

import java.util.List;

public interface FcmDummyRepository {
    void batchInsertFcms(List<Fcm> fcms, int batchSize);
}
