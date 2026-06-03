package org.ping_me;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableMongoAuditing
@EnableWebSocketMessageBroker
@EnableJpaRepositories(basePackages = "org.ping_me.repository.jpa")
@EnableMongoRepositories(basePackages = "org.ping_me.repository.mongodb")
@EnableFeignClients
@EnableAsync
@EnableScheduling
@EnableMethodSecurity
public class PingMeCoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PingMeCoreServiceApplication.class, args);
    }

}
