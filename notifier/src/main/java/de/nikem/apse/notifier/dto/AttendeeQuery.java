package de.nikem.apse.notifier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendeeQuery {

    private String id;

    private String firstName;
    private String email;

    private String eventId;
    private String eventName;

    private LocalDateTime startDateTime;
    private ZoneId zoneId;

    private LocalDateTime decisionDateTime;
}
