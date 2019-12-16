package de.nikem.apse.data.repository;

import de.nikem.apse.data.entitiy.EventDefinitionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface EventDefinitionRepository extends ReactiveCrudRepository<EventDefinitionEntity, String> {
    Flux<EventDefinitionEntity> findByQueryDateTimeBeforeAndActiveIsTrue(LocalDateTime now);
}
