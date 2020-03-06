package de.nikem.apse.notifier.service;

import de.nikem.apse.notifier.dto.AttendeeQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApseMailSender {

    private final JavaMailSender javaMailSender;

    public void sendQuery(AttendeeQuery attendeeQuery) {
        log.debug("send mail to {}", attendeeQuery);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(attendeeQuery.getEmail());
        message.setFrom("apse@nikem.eu");
        message.setSubject("Einladung: " + attendeeQuery.getEventName());
        message.setText("Hi " + attendeeQuery.getFirstName() + ",\n" +
                "\n" +
                "Einladung zum " + attendeeQuery.getEventName() +
                " am " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(attendeeQuery.getStartDateTime()) +
                ".\n" +
                "\n" +
                "Bist du dabei?\n" +
                //"[ JA ] [nein]\n" +
                "\n" +
                "Gruß\n" +
                "Dein Team");
        try {
            javaMailSender.send(message);
        } catch (Exception ex) {
            log.error("error sending mail to " + attendeeQuery, ex);
        }
    }
}
