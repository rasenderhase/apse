package de.nikem.apse.data.entitiy;

import de.nikem.apse.data.enums.AttendeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("EventAttendee")
public class EventAttendeeEntity implements Cloneable {
    @Id
    private String id;

    private String firstName;
    private String email;

    @Builder.Default
    private boolean active = true;
    @Builder.Default
    private AttendeeStatus attendeeStatus = AttendeeStatus.IDLE;

    public EventAttendeeEntity(EventAttendeeEntity toCopy) {
        id = new ObjectId(new Date(), (int) (Math.random() * 16_777_215)).toString();
        firstName = toCopy.firstName;
        email = toCopy.email;
        active = toCopy.active;
        attendeeStatus = toCopy.attendeeStatus;
    }
}
