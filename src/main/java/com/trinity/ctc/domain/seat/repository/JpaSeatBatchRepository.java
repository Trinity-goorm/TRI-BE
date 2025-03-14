package com.trinity.ctc.domain.seat.repository;

import com.trinity.ctc.domain.seat.entity.Seat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaSeatBatchRepository implements SeatBatchRepository {

    @PersistenceContext
    private final EntityManager entityManager;


    @Override
    @Transactional
    public void batchInsertSeats(List<Seat> seats, int batchSize) {
        int totalSize = seats.size();
        log.info("🚀 INSERT 할 Seat 데이터 개수: {}", totalSize);

        for (int i = 0; i < totalSize; i++) {
            entityManager.persist(seats.get(i));

            if ((i + 1) % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();
    }
}
