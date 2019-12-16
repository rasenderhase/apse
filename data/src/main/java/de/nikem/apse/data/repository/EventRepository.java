package de.nikem.apse.data.repository;

import de.nikem.apse.data.entitiy.EventEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends ReactiveCrudRepository<EventEntity, String> {
}
