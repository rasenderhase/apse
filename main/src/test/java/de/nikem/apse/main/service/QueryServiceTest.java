package de.nikem.apse.main.service;

import de.nikem.apse.data.entitiy.EventDefinitionEntity;
import de.nikem.apse.data.entitiy.EventEntity;
import de.nikem.apse.data.repository.EventDefinitionRepository;
import de.nikem.apse.data.repository.EventRepository;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;

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
        queryService.processQueries().subscribe();

        ArgumentCaptor<EventDefinitionEntity> definitionCaptor = ArgumentCaptor.forClass(EventDefinitionEntity.class);
        verify(eventDefinitionRepository).save(definitionCaptor.capture());
        final EventDefinitionEntity capturedDefinition = definitionCaptor.getValue();
        MatcherAssert.assertThat(capturedDefinition, Matchers.hasProperty("eventName", Matchers.is("Kick")));

        ArgumentCaptor<EventEntity> eventCaptor = ArgumentCaptor.forClass(EventEntity.class);
        verify(eventRepository).save(eventCaptor.capture());
        final EventEntity capturedEvent = eventCaptor.capture();
        MatcherAssert.assertThat(capturedDefinition, Matchers.hasProperty("eventName", Matchers.is("Kick")));
    }
}