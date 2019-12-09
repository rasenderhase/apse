package de.nikem.apse.data.entitiy;

import de.nikem.apse.data.enums.AttendeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("EventAttendee")
public class EventAttendeeEntity {
    @Id
    private String id;

    private String firstName;
    private String email;

    @Builder.Default
    private boolean active = true;
    @Builder.Default
    private AttendeeStatus attendeeStatus = AttendeeStatus.IDLE;
}
