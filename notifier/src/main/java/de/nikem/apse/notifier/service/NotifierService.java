package de.nikem.apse.notifier.service;

import de.nikem.apse.data.entitiy.EventAttendeeEntity;
import de.nikem.apse.data.entitiy.EventEntity;
import de.nikem.apse.data.enums.AttendeeStatus;
import de.nikem.apse.data.repository.EventRepository;
import de.nikem.apse.notifier.dto.AttendeeQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class NotifierService {
    private final EventRepository eventRepository;
    private final Clock clock;
    private final ApseMailSender mailSender;

    public Flux<? extends AttendeeQuery> queryAttendees() {
        return eventRepository.findUnqueried(LocalDateTime.now(clock))
                .log()
                .map(eventEntity -> {
                    log.debug("event in status INVITATION: {}", eventEntity);
                    return eventEntity;
                })
                .flatMap(this::findAndMarkAttendees)
                .map(this::sendNotifications);
    }

    private Flux<? extends AttendeeQuery> findAndMarkAttendees(EventEntity eventEntity) {
        final List<EventAttendeeEntity> atendeesToInvite = eventEntity.getAttendees()
                .stream()
                .filter(EventAttendeeEntity::isActive)
                .filter(eventAttendeeEntity -> eventAttendeeEntity.getAttendeeStatus() == AttendeeStatus.IDLE)
                .collect(Collectors.toList());
        atendeesToInvite.forEach(eventAttendeeEntity -> eventAttendeeEntity.setAttendeeStatus(AttendeeStatus.INVITED));

        return eventRepository.save(eventEntity)
                .flux()
                .map(ev -> atendeesToInvite.toArray(new EventAttendeeEntity[0]))
                .flatMap(Flux::just)
                .map(eventAttendeeEntity -> AttendeeQuery.builder()
                        .decisionDateTime(eventEntity.getDecisionDateTime())
                        .email(eventAttendeeEntity.getEmail())
                        .eventId(eventEntity.getId())
                        .eventName(eventEntity.getEventName())
                        .firstName(eventAttendeeEntity.getFirstName())
                        .id(eventAttendeeEntity.getId())
                        .startDateTime(eventEntity.getStartDateTime())
                        .zoneId(eventEntity.getZoneId())
                        .build());
    }

    private AttendeeQuery sendNotifications(AttendeeQuery attendeeQuery) {
        mailSender.sendQuery(attendeeQuery);
        return attendeeQuery;
    }
}
