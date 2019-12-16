package de.nikem.apse.data.entitiy;

import de.nikem.apse.data.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("Event")
public class EventEntity {
    @Id
    private String id;

    private String eventDefinitionId;

    private String eventName;

    private LocalDateTime startDateTime;
    private ZoneId zoneId;

    private LocalDateTime queryDateTime;
    private LocalDateTime decisionDateTime;

    private int minimumAttendees;

    private Collection<EventAttendeeEntity> attendees;
    @Builder.Default
    private EventStatus eventStatus = EventStatus.INVITATION;
}
