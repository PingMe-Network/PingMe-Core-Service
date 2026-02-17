package me.huynhducphu.ping_me.config.integration;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Admin 8/16/2025
 **/
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class S3Config {

    @Value("${aws.access-key}")
    String accessKey;

    @Value("${aws.secret-key}")
    String secretKey;

    @Value("${aws.region}")
    String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    private StaticCredentialsProvider getCredentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

}
