package de.nikem.apse.notifier.service;

import de.nikem.apse.data.entitiy.EventDefinitionEntity;
import de.nikem.apse.data.repository.EventDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class NotifierService {
    private final EventDefinitionRepository eventDefinitionRepository;

    public Flux<EventDefinitionEntity> createEvents() {
        final Flux<EventDefinitionEntity> allEventDefinitions = eventDefinitionRepository.findAll();

        return allEventDefinitions;
    }

    public void queryAttendees() {

    }

}
