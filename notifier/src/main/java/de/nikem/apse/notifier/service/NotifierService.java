package de.nikem.apse.notifier.service;

import de.nikem.apse.data.enums.EventStatus;
import de.nikem.apse.data.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifierService {
    private final EventRepository eventRepository;
    private final Clock clock;

    public void queryAttendees() {
        eventRepository.findUnqueried(LocalDateTime.now(clock), EventStatus.INVITATION)
        .log()
        .subscribe(eventEntity -> log.debug("event in status INVITATION: {}", eventEntity));
    }

}
