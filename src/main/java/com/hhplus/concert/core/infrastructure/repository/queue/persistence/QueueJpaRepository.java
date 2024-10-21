package com.hhplus.concert.core.infrastructure.repository.queue.persistence;

import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueJpaRepository extends JpaRepository<Queue, Long> {
    List<Queue> findAllByStatusOrderByIdDesc(QueueStatus queueStatus);

    Optional<Queue> findByToken(String token);

    @Query("""
               SELECT count(1) FROM Queue q
               WHERE q.status =:status
               AND q.enteredDt < :enteredDt
            """)
    Long findStatusIsWaitingAndAlreadyEnteredBy(
            @Param("enteredDt") LocalDateTime enteredDt,
            @Param("status") QueueStatus status
    );

    @Query("""
               SELECT q FROM Queue q
               WHERE q.userId =:userId
               AND (q.status = "WAITING" OR q.status = "PROGRESS")
               ORDER BY q.enteredDt DESC
               LIMIT 1
            """)
    Optional<Queue> findByUserIdForWaitingOrProgress(
            @Param("userId") Long userId
    );

    @Modifying
    @Query("""
                 UPDATE Queue q SET q.status = :updateStatus
                 WHERE q.status =:conditionStatus AND q.enteredDt < :conditionExpiredAt
            """)
    void updateStatusExpire(
            @Param("updateStatus") QueueStatus updateStatus,
            @Param("conditionStatus") QueueStatus conditionStatus,
            @Param("conditionExpiredAt") LocalDateTime conditionExpiredAt
    );

    @Query("""
            SELECT COUNT(1) FROM Queue q WHERE q.status = :queueStatus
        """)
    int countByStatus(@Param("queueStatus") QueueStatus queueStatus);

    @Query("""
            SELECT q FROM Queue q WHERE q.status = :status ORDER BY q.enteredDt LIMIT :remainingSlots
        """)
    List<Queue> findTopNWaiting(
            @Param("status") QueueStatus status,
            @Param("remainingSlots") int remainingSlots
    );

    @Modifying
    @Query("""
            UPDATE Queue SET status = :status WHERE id IN (:ids)
        """)
    void updateStatusByIds(
            @Param("ids") List<Long> ids,
            @Param("status") QueueStatus status
    );
}
