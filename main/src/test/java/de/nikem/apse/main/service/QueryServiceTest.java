package de.nikem.apse.main.service;

import de.nikem.apse.data.entitiy.EventDefinitionEntity;
import de.nikem.apse.data.entitiy.EventEntity;
import de.nikem.apse.data.repository.EventDefinitionRepository;
import de.nikem.apse.data.repository.EventRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

class QueryServiceTest {

    public static LocalDateTime testLocalDateTime(String text) {
        return LocalDateTime.ofInstant(Instant.parse(text), ZoneId.of("Europe/Berlin"));
    }

    private QueryService queryService;

    @Mock
    private EventDefinitionRepository eventDefinitionRepository;
    @Mock
    private EventRepository eventRepository;
    private Clock clock = Clock.fixed(Instant.parse("2019-12-17T08:03:00.00Z"), ZoneId.of("Europe/Berlin"));

    private EventDefinitionEntity eventDefinitionEntity;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        queryService = new QueryService(clock, eventDefinitionRepository, eventRepository);

        eventDefinitionEntity = EventDefinitionEntity.builder()
                .active(true)
                .decisionDateTime(testLocalDateTime("2019-12-17T16:00:00.00Z"))
                .durationDecisionBeforeEvent(Duration.parse("PT4H"))
                .durationQueryBeforeEvent(Duration.parse("PT12H"))
                .eventName("Kick")
                .id("1234")
                .interval(Duration.parse("P7D"))
                .minimumAttendees(2)
                .queryDateTime(testLocalDateTime("2019-12-17T08:00:00.00Z"))
                .startDateTime(testLocalDateTime("2019-12-17T20:00:00.00Z"))
                .zoneId(ZoneId.of("Europe/Berlin"))
                .build();

        when(eventDefinitionRepository.findByQueryDateTimeBeforeAndActiveIsTrue(LocalDateTime.now(clock)))
                .thenReturn(Flux.just(eventDefinitionEntity));
        when(eventDefinitionRepository.save(eventDefinitionEntity)).thenReturn(Mono.just(eventDefinitionEntity));
        when(eventRepository.save(any())).then(i -> {
            final EventEntity event = i.getArgument(0);
            event.setId("4711");
            return Mono.just(event);
        });
    }

    @org.junit.jupiter.api.Test
    void processQueries() {
        final Flux<EventEntity> eventEntityFlux = queryService.processQueries();
        //Consume the Flux
        Collection<EventEntity> events = eventEntityFlux.toStream().collect(Collectors.toList());

        verify(eventDefinitionRepository).save(eventDefinitionEntity);
        assertThat(eventDefinitionEntity, hasProperty("eventName", is("Kick")));

        verify(eventRepository).save(any());
        assertThat(events, hasSize(1));
        EventEntity event = events.stream().findFirst().orElseThrow();
        assertThat(event, hasProperty("eventName", is("Kick")));
    }
}