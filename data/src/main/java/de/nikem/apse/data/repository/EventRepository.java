package de.nikem.apse.data.repository;

import de.nikem.apse.data.entitiy.EventEntity;
import de.nikem.apse.data.enums.EventStatus;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface EventRepository extends ReactiveCrudRepository<EventEntity, String> {

    @Query("{queryDateTime: {$lt: { $date : ?0}}, decisionDateTime: {$gt: { $date : ?0}}, eventStatus: ?1 }")
    Flux<EventEntity> findUnqueried(LocalDateTime now,
                                    EventStatus eventStatus);
}
