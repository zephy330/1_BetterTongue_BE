package com.example.bettertalk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
class BetterTalkApplication

fun main(args: Array<String>) {
    runApplication<BetterTalkApplication>(*args)
}
