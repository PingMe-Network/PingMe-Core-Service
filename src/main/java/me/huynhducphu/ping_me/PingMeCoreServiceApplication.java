package me.huynhducphu.ping_me;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableMongoAuditing
@EnableWebSocketMessageBroker
@EnableCaching
@EnableJpaRepositories(basePackages = "me.huynhducphu.ping_me.repository.jpa")
@EnableMongoRepositories(basePackages = "me.huynhducphu.ping_me.repository.mongodb")
@EnableFeignClients
public class PingMeCoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PingMeCoreServiceApplication.class, args);
    }

}
