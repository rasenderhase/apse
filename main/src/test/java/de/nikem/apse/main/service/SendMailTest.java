package de.nikem.apse.main.service;

import de.nikem.apse.notifier.dto.AttendeeQuery;
import de.nikem.apse.notifier.service.ApseMailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@SpringBootTest
@AutoConfigureTestDatabase
public class SendMailTest {

    private static final Instant INSTANT = Instant.parse("2019-12-17T08:03:00.00Z");
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");

    @TestConfiguration
    public static class Configuration {

        @Bean
        public Clock clock() {
            return Clock.fixed(INSTANT, ZONE_ID);
        }
    }

    @Autowired
    private ApseMailSender apseMailSender;

    @Test
    public void testMail() {
        AttendeeQuery query = AttendeeQuery.builder()
                .startDateTime(LocalDateTime.ofInstant(INSTANT, ZONE_ID))
                .firstName("Andi")
                .email("andreas@knees.de")
                .eventName("Test Event")
                .build();
        apseMailSender.sendQuery(query);
    }
}
