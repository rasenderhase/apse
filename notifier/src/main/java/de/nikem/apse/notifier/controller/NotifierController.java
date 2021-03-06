package de.nikem.apse.notifier.controller;

import de.nikem.apse.notifier.service.NotifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifierController {

    private final NotifierService notifierService;

    @Scheduled(fixedRate = 60_000, initialDelay = 1000)
    public void schedule() {
        log.info("execute schedule");

        notifierService.queryAttendees().subscribe(aq -> log.debug("attendee query: {}", aq));
    }
}
