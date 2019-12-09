package de.nikem.apse.data.repository;

import de.nikem.apse.data.entitiy.EventDefinitionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventDefinitionRepository extends ReactiveCrudRepository<EventDefinitionEntity, String> {
}
