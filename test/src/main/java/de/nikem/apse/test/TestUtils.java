package de.nikem.apse.test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TestUtils {
    public static LocalDateTime testLocalDateTime(String text) {
        return LocalDateTime.ofInstant(Instant.parse(text), ZoneId.of("Europe/Berlin"));
    }
}
