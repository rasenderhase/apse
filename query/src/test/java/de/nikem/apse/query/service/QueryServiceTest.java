package de.nikem.apse.query.service;

import de.nikem.apse.data.entitiy.EventAttendeeEntity;
import de.nikem.apse.data.entitiy.EventDefinitionEntity;
import de.nikem.apse.data.entitiy.EventEntity;
import de.nikem.apse.data.enums.AttendeeStatus;
import de.nikem.apse.data.enums.EventStatus;
import de.nikem.apse.data.repository.EventDefinitionRepository;
import de.nikem.apse.data.repository.EventRepository;
import de.nikem.apse.test.TestUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

class QueryServiceTest {

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
                .decisionDateTime(TestUtils.testLocalDateTime("2019-12-17T16:00:00.00Z"))
                .durationDecisionBeforeEvent(Duration.parse("PT4H"))
                .durationQueryBeforeEvent(Duration.parse("PT12H"))
                .eventName("Kick")
                .id("1234")
                .interval(Duration.parse("P7D"))
                .minimumAttendees(2)
                .queryDateTime(TestUtils.testLocalDateTime("2019-12-17T08:00:00.00Z"))
                .startDateTime(TestUtils.testLocalDateTime("2019-12-17T20:00:00.00Z"))
                .zoneId(ZoneId.of("Europe/Berlin"))
                .attendeeDefinitions(Arrays.asList(EventAttendeeEntity.builder()
                                .active(true)
                                .email("rea@knees.de")
                                .build(),
                        EventAttendeeEntity.builder()
                                .active(true)
                                .email("tom@knees.de")
                                .build(),
                        EventAttendeeEntity.builder()
                                .active(true)
                                .email("ben@knees.de")
                                .build()))
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
        final Flux<EventEntity> eventEntityFlux = queryService.createEvents();
        //Consume the Flux
        eventEntityFlux.subscribe();

        verify(eventDefinitionRepository).save(eventDefinitionEntity);
        assertThat(eventDefinitionEntity, hasProperty("eventName", is("Kick")));
        assertThat(eventDefinitionEntity, hasProperty("startDateTime", is(TestUtils.testLocalDateTime("2019-12-24T20:00:00.00Z"))));

        ArgumentCaptor<EventEntity> eventEntityArgumentCaptor = ArgumentCaptor.forClass(EventEntity.class);
        verify(eventRepository).save(eventEntityArgumentCaptor.capture());
        Collection<EventEntity> events = eventEntityArgumentCaptor.getAllValues();
        assertThat("one event has been created", events, hasSize(1));
        EventEntity event = events.stream().findFirst().orElseThrow();
        assertThat(event, hasProperty("eventName", is("Kick")));
        assertThat(event, hasProperty("eventStatus", is(EventStatus.INVITATION)));
        assertThat(event, hasProperty("startDateTime", is(TestUtils.testLocalDateTime("2019-12-17T20:00:00.00Z"))));

        final Map<String, EventAttendeeEntity> attendees = event.getAttendees();
        assertThat(attendees.values(), hasSize(3));
        attendees.values().forEach(attendee -> assertThat(attendee, hasProperty("attendeeStatus", is(AttendeeStatus.IDLE))));
        attendees.values().forEach(attendee -> assertThat(attendee, hasProperty("id", is(notNullValue()))));
        assertThat("ben is an attendee", attendees.values().stream().anyMatch(a -> a.getEmail().equals("ben@knees.de")), is(true));
    }

    @org.junit.jupiter.api.Test
    void processQueriesDecisionInThePast() {
        // decision time of event definition is in the past -> no event must be created
        eventDefinitionEntity.setDecisionDateTime(TestUtils.testLocalDateTime("2019-12-17T08:00:00.00Z"));

        queryService.createEvents().subscribe();

        assertThat("shift event definition to current date", eventDefinitionEntity, hasProperty("startDateTime", is(TestUtils.testLocalDateTime("2019-12-24T20:00:00.00Z"))));
        assertThat("shift event decision to current date", eventDefinitionEntity, hasProperty("decisionDateTime", is(TestUtils.testLocalDateTime("2019-12-24T16:00:00.00Z"))));
        assertThat("shift event decision to current date", eventDefinitionEntity, hasProperty("queryDateTime", is(TestUtils.testLocalDateTime("2019-12-24T08:00:00.00Z"))));
        verify(eventRepository, never()).save(any());
    }

    @org.junit.jupiter.api.Test
    void processQueriesStartTimeFarInThePast() {
        eventDefinitionEntity.setQueryDateTime(   TestUtils.testLocalDateTime("2019-12-10T08:00:00.00Z"));
        eventDefinitionEntity.setDecisionDateTime(TestUtils.testLocalDateTime("2019-12-10T16:00:00.00Z"));
        eventDefinitionEntity.setStartDateTime(   TestUtils.testLocalDateTime("2019-12-10T20:00:00.00Z"));

        queryService.createEvents().subscribe();
        //assertThat("shift event definition to current date", eventDefinitionEntity, hasProperty("startDateTime", is(testLocalDateTime("2019-12-17T20:00:00.00Z"))));
        //assertThat("shift event decision to current date", eventDefinitionEntity, hasProperty("decisionDateTime", is(testLocalDateTime("2019-12-17T16:00:00.00Z"))));
        //assertThat("shift event decision to current date", eventDefinitionEntity, hasProperty("queryDateTime", is(testLocalDateTime("2019-12-17T08:00:00.00Z"))));
        // skip the event - as it's decision date is already in the past

        queryService.createEvents().subscribe();
        assertThat("shift event definition to next date", eventDefinitionEntity, hasProperty("startDateTime", is(TestUtils.testLocalDateTime("2019-12-24T20:00:00.00Z"))));
        assertThat("shift event decision to next date", eventDefinitionEntity, hasProperty("decisionDateTime", is(TestUtils.testLocalDateTime("2019-12-24T16:00:00.00Z"))));
        assertThat("shift event decision to next date", eventDefinitionEntity, hasProperty("queryDateTime", is(TestUtils.testLocalDateTime("2019-12-24T08:00:00.00Z"))));
        //for THIS, an event should be created

        queryService.createEvents().subscribe();
        queryService.createEvents().subscribe();
        queryService.createEvents().subscribe();
        assertThat("nothing to be shifted any more", eventDefinitionEntity, hasProperty("startDateTime", is(TestUtils.testLocalDateTime("2019-12-24T20:00:00.00Z"))));
        assertThat("nothing to be shifted any more", eventDefinitionEntity, hasProperty("decisionDateTime", is(TestUtils.testLocalDateTime("2019-12-24T16:00:00.00Z"))));
        assertThat("nothing to be shifted any more", eventDefinitionEntity, hasProperty("queryDateTime", is(TestUtils.testLocalDateTime("2019-12-24T08:00:00.00Z"))));
        //do not create event instances before query date.

        ArgumentCaptor<EventEntity> eventEntityArgumentCaptor = ArgumentCaptor.forClass(EventEntity.class);
        verify(eventRepository).save(eventEntityArgumentCaptor.capture());
        Collection<EventEntity> events = eventEntityArgumentCaptor.getAllValues();
        assertThat("one event has been created", events, hasSize(1));
        EventEntity event = events.stream().findFirst().orElseThrow();
        assertThat(event, hasProperty("startDateTime", is(TestUtils.testLocalDateTime("2019-12-17T20:00:00.00Z"))));
    }

    @org.junit.jupiter.api.Test
    void processQueriesSingleEvent() {
        eventDefinitionEntity.setInterval(null);

        queryService.createEvents().subscribe();

        assertThat("single event has been deactivated.", eventDefinitionEntity, hasProperty("active", is(false)));
    }

    @org.junit.jupiter.api.Test
    void processQueriesNotActiveEvent() {
        eventDefinitionEntity.setActive(false);

        queryService.createEvents().subscribe();

        assertThat("event is still deactivated.", eventDefinitionEntity, hasProperty("active", is(false)));
        verify(eventRepository, never()).save(any(EventEntity.class));
    }
}