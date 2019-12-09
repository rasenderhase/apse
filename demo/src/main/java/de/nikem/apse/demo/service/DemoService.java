package de.nikem.apse.demo.service;

import de.nikem.apse.data.entitiy.EventAttendeeEntity;
import de.nikem.apse.data.entitiy.EventDefinitionEntity;
import de.nikem.apse.data.repository.EventDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DemoService {
    private final EventDefinitionRepository eventDefinitionRepository;

    public final Mono<EventDefinitionEntity> create() {
        return eventDefinitionRepository.save(EventDefinitionEntity.builder()
                .eventName("Test Event")
                .interval(Duration.of(4, ChronoUnit.MINUTES))
                .startDateTime(LocalDateTime.ofInstant(Instant.parse("2019-10-13T10:15:30.00Z"), ZoneId.of("Europe/Berlin")))
                .durationDecisionBeforeEvent(Duration.ofMinutes(1))
                .durationQueryBeforeEvent(Duration.ofMinutes(3))
                .minimumAttendees(1)
                .attendeeDefinitions(Stream.of(EventAttendeeEntity.builder()
                        .active(true)
                        .email("andreas@knees.de")
                        .firstName("Andi")
                        .build(),
                        EventAttendeeEntity.builder()
                                .active(true)
                                .email("andreas.nikem@googlemail.com")
                                .firstName("Andreas")
                                .build())
                .collect(Collectors.toSet()))
                .build());
    }
}
