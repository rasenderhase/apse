package de.nikem.apse.notifier.service;

import de.nikem.apse.data.entitiy.EventAttendeeEntity;
import de.nikem.apse.data.entitiy.EventEntity;
import de.nikem.apse.data.enums.AttendeeStatus;
import de.nikem.apse.data.repository.EventRepository;
import de.nikem.apse.notifier.dto.AttendeeQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.nikem.apse.test.TestUtils.testLocalDateTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class NotifierServiceTest {

    @Mock
    private EventRepository eventRepository;
    private Clock clock = Clock.fixed(Instant.parse("2019-12-17T08:03:00.00Z"), ZoneId.of("Europe/Berlin"));

    private NotifierService notifierService;

    private EventEntity event;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        event = EventEntity.builder()
                .decisionDateTime(testLocalDateTime("2019-12-17T16:00:00.00Z"))
                .eventName("Kick")
                .id("1234")
                .minimumAttendees(2)
                .queryDateTime(testLocalDateTime("2019-12-17T08:00:00.00Z"))
                .startDateTime(testLocalDateTime("2019-12-17T20:00:00.00Z"))
                .zoneId(ZoneId.of("Europe/Berlin"))
                .attendees(Arrays.asList(EventAttendeeEntity.builder()
                                .id("3")
                                .active(true)
                                .email("rea@knees.de")
                                .attendeeStatus(AttendeeStatus.INVITED)
                                .build(),
                        EventAttendeeEntity.builder()
                                .id("1")
                                .active(true)
                                .email("tom@knees.de")
                                .build(),
                        EventAttendeeEntity.builder()
                                .id("2")
                                .active(true)
                                .email("ben@knees.de")
                                .build()))
                .build();
        Mockito.when(eventRepository.findUnqueried(LocalDateTime.now(clock))).thenReturn(Flux.just(event));
        Mockito.when(eventRepository.save(event)).thenReturn(Mono.just(event));

        notifierService = new NotifierService(eventRepository, clock);
    }

    @Test
    void testQueryAnttendees() {
        final List<? extends AttendeeQuery> attendeeQueries = notifierService.queryAttendees().toStream().collect(Collectors.toList());
        event.getAttendees().forEach(a -> assertThat(a, hasProperty("attendeeStatus", is(AttendeeStatus.INVITED))));
        assertThat(attendeeQueries, hasSize(2));
    }
}