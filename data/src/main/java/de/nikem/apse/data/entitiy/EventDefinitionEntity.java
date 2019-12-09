package de.nikem.apse.data.entitiy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.*;
import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("EventDefinition")
public class EventDefinitionEntity {
    @Id
    private String id;

    private String eventName;

    private LocalDateTime startDateTime;
    private ZoneId zoneId;
    private Duration interval;

    private Duration durationQueryBeforeEvent;
    private Duration durationDecisionBeforeEvent;

    private int minimumAttendees;

    @Builder.Default
    private boolean active = true;

    private Collection<EventAttendeeEntity> attendeeDefinitions;
}
