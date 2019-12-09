package de.nikem.apse.demo.controller;

import de.nikem.apse.data.entitiy.EventDefinitionEntity;
import de.nikem.apse.demo.dto.Demo;
import de.nikem.apse.demo.service.DemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController("/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;

    @GetMapping("/demo")
    Flux<Demo> demo() {
        return Flux.just(Demo.builder()
                        .greeting("Hello World, today is")
                        .date(LocalDate.now())
                        .build(),
                Demo.builder()
                        .greeting("Tomorrow will be")
                        .date(LocalDate.now().plus(1, ChronoUnit.DAYS))
                        .build()
        ).log();
    }

    @GetMapping("/create")
    final Mono<EventDefinitionEntity> create() {
        return demoService.create();
    }

}
