package de.nikem.apse.main.service;

import de.nikem.apse.data.entitiy.EventDefinitionEntity;
import de.nikem.apse.data.entitiy.EventEntity;
import de.nikem.apse.data.repository.EventDefinitionRepository;
import de.nikem.apse.data.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QueryService {
    private final Clock clock;
    private final EventDefinitionRepository eventDefinitionRepository;
    private final EventRepository eventRepository;

    public Flux<EventEntity> processQueries() {
        return eventDefinitionRepository.findByQueryDateTimeBeforeAndActiveIsTrue(LocalDateTime.now(clock))
                .map(this::createEvent)
                .flatMap(this::persist);
    }

    private Tuple2<EventDefinitionEntity, EventEntity> createEvent(EventDefinitionEntity eventDefinition) {
        EventEntity event = EventEntity.builder()
                .decisionDateTime(eventDefinition.getDecisionDateTime())
                .eventDefinitionId(eventDefinition.getId())
                .eventName(eventDefinition.getEventName())
                .minimumAttendees(eventDefinition.getMinimumAttendees())
                .queryDateTime(eventDefinition.getQueryDateTime())
                .startDateTime(eventDefinition.getStartDateTime())
                .zoneId(eventDefinition.getZoneId())
                .build();
        updateToNextEventDefinition(eventDefinition);
        return Tuples.of(eventDefinition, event);
    }

    private Mono<EventEntity> persist(Tuple2<EventDefinitionEntity, EventEntity> tuple) {
        EventDefinitionEntity eventDefinition = tuple.getT1();
        EventEntity event = tuple.getT2();
        return eventDefinitionRepository.save(eventDefinition)
                .then(eventRepository.save(event));
    }

    private void updateToNextEventDefinition(EventDefinitionEntity eventDefinition) {
        if (eventDefinition.getInterval() != null) {
            calculateNextStartTime(eventDefinition);
        }
    }

    private void calculateNextStartTime(EventDefinitionEntity eventDefinition) {
        eventDefinition.setStartDateTime(eventDefinition.getStartDateTime().plus(eventDefinition.getInterval()));
        eventDefinition.setQueryDateTime(eventDefinition.getStartDateTime().minus(eventDefinition.getDurationQueryBeforeEvent()));
        eventDefinition.setDecisionDateTime(eventDefinition.getStartDateTime().minus(eventDefinition.getDurationDecisionBeforeEvent()));
    }
}
