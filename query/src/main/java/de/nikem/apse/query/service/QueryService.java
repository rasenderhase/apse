package de.nikem.apse.query.service;

import de.nikem.apse.data.entitiy.EventAttendeeEntity;
import de.nikem.apse.data.entitiy.EventDefinitionEntity;
import de.nikem.apse.data.entitiy.EventEntity;
import de.nikem.apse.data.repository.EventDefinitionRepository;
import de.nikem.apse.data.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {
    private final Clock clock;
    private final EventDefinitionRepository eventDefinitionRepository;
    private final EventRepository eventRepository;

    /**
     * select all event definitions with <code>queryDateTime</code> in the past and create the events and attendees.
     *
     * @return All events that have been created.
     */
    public Flux<EventEntity> processQueries() {
        return eventDefinitionRepository.findByQueryDateTimeBeforeAndActiveIsTrue(LocalDateTime.now(clock))
                .log()
                .map(this::createEvent)
                .log()
                .flatMap(this::persist);
    }

    private Tuple2<EventDefinitionEntity, Optional<EventEntity>> createEvent(EventDefinitionEntity eventDefinition) {
        final Optional<EventEntity> event;
        final LocalDateTime now = LocalDateTime.now(clock);
        log.debug("evaluate query date: \nnow           {}\nquery date    {}\ndecision date {}",
                now, eventDefinition.getQueryDateTime(), eventDefinition.getDecisionDateTime());
        if (eventDefinition.getQueryDateTime().isBefore(now)
            && eventDefinition.getDecisionDateTime().isAfter(now)) {
            log.info("create event for event definition id={}\nnow           {}\nquery date    {}\ndecision date {}",
                    eventDefinition.getId(), now, eventDefinition.getQueryDateTime(), eventDefinition.getDecisionDateTime());
            event = Optional.of(
                    EventEntity.builder()
                            .decisionDateTime(eventDefinition.getDecisionDateTime())
                            .eventDefinitionId(eventDefinition.getId())
                            .eventName(eventDefinition.getEventName())
                            .minimumAttendees(eventDefinition.getMinimumAttendees())
                            .queryDateTime(eventDefinition.getQueryDateTime())
                            .startDateTime(eventDefinition.getStartDateTime())
                            .zoneId(eventDefinition.getZoneId())
                            .attendees(eventDefinition.getAttendeeDefinitions().stream()
                                    .map(EventAttendeeEntity::new)
                                    .collect(Collectors.toList()))
                            .build()
            );
        } else {
            log.debug("now is not after query date and before decision date - create nothing");
            event = Optional.empty();
        }
        updateToNextEventDefinition(eventDefinition);
        return Tuples.of(eventDefinition, event);
    }

    private Mono<EventEntity> persist(Tuple2<EventDefinitionEntity, Optional<EventEntity>> tuple) {
        EventDefinitionEntity eventDefinition = tuple.getT1();
        Optional<EventEntity> event = tuple.getT2();
        final Mono<EventDefinitionEntity> eventDefinitionEntityMono = eventDefinitionRepository
                .save(eventDefinition)
                .log();
        return event.map(e -> eventDefinitionEntityMono
                .then(eventRepository.save(e)))
                .orElse(eventDefinitionEntityMono.then(Mono.empty()))
                .log();
    }

    private void updateToNextEventDefinition(EventDefinitionEntity eventDefinition) {
        final LocalDateTime now = LocalDateTime.now(clock);
        if (eventDefinition.getInterval() != null && eventDefinition.getQueryDateTime().isBefore(now)) {
            calculateNextStartTime(eventDefinition);
        } else {
            eventDefinition.setActive(false);
        }
    }

    private void calculateNextStartTime(EventDefinitionEntity eventDefinition) {
        eventDefinition.setStartDateTime(eventDefinition.getStartDateTime().plus(eventDefinition.getInterval()));
        eventDefinition.setQueryDateTime(eventDefinition.getStartDateTime().minus(eventDefinition.getDurationQueryBeforeEvent()));
        eventDefinition.setDecisionDateTime(eventDefinition.getStartDateTime().minus(eventDefinition.getDurationDecisionBeforeEvent()));
        log.info("update dates of event definition id={}:\nquery date    {}\ndecision date {}\nstart date    {}",
                eventDefinition.getId(),
                eventDefinition.getQueryDateTime(),
                eventDefinition.getDecisionDateTime(),
                eventDefinition.getStartDateTime());
    }
}
