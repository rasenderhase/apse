package de.nikem.apse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Locale;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        Locale.setDefault(Locale.GERMANY);
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        log.info("Set locale {} and server timezone {}", Locale.getDefault(), TimeZone.getDefault());
        SpringApplication.run(Application.class, args);
    }
}
