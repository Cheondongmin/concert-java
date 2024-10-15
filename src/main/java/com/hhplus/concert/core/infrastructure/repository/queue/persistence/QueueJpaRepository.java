package com.hhplus.concert.core.infrastructure.repository.queue.persistence;

import com.hhplus.concert.core.domain.queue.entlty.Queue;
import com.hhplus.concert.core.domain.queue.entlty.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
