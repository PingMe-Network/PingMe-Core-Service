# PingMe Core Service

Core backend service for the PingMe Network. Built with Spring Boot and provides REST APIs, WebSocket messaging, data persistence (MariaDB + MongoDB), caching (Redis), and integrations like S3 and AI providers.

## Tech Stack
- Java 21, Spring Boot 4
- Spring Web MVC, Security (OAuth2 Resource Server), Validation
- Spring Data JPA (MariaDB), Spring Data MongoDB
- Spring Cache + Redis
- WebSocket (STOMP)
- OpenFeign, Resilience4j
- Spring AI (OpenAI), Groq AI
- AWS SDK (S3)

## Requirements
- JDK 21
- Maven 3.9+
- MariaDB, MongoDB, Redis
- Optional: AWS credentials for S3

## Configuration
The app uses environment variables via `application.properties`. At minimum you need database and security settings.

Example `.env` (values are placeholders):
```env
SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3306/pingme
SPRING_DATASOURCE_USERNAME=pingme
SPRING_DATASOURCE_PASSWORD=secret

SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/pingme

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

JWT_SECRET=change-me
MESSAGES_AES_KEY=change-me
CORS_ALLOWED_ORIGINS=http://localhost:3000

AWS_ACCESS_KEY=...
AWS_SECERT_KEY=...
AWS_REGION=ap-southeast-1
AWS_S3_BUCKET_NAME=...
AWS_S3_DOMAIN=...

WEATHER_API_BASE_URL=...
WEATHER_API_KEY=...

SPRING_AI_OPENAI_API_KEY=...
SPRING_AI_OPENAI_CHAT_MODEL=...
GROQ_AI_API_KEY=...
GROQ_AI_API_URL=...

MAIL_SERVICE_URL=http://localhost:8081
MAIL_DEFAULT_OTP=000000

APP_REELS_MAX_VIDEO_SIZE=...
APP_REELS_FOLDER=...
APP_MESSAGES_CACHE_ENABLED=true
APP_INTERNAL_SECRET=...
```

## Run Locally
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Default port: `8080`.

Health check:
```
GET /actuator/health
```

## Build
```bash
./mvnw -DskipTests package
```

Run the jar:
```bash
java -jar target/pingme-core-service-0.0.1-SNAPSHOT.jar
```

## Docker Image (Jib)
Build and push to Docker Hub:
```bash
./mvnw -DskipTests clean compile jib:build \
  -Djib.to.auth.username=YOUR_DOCKER_USERNAME \
  -Djib.to.auth.password=YOUR_DOCKER_PASSWORD
```

## CI/CD
GitHub Actions builds and pushes a Docker image using Jib, then deploys to AWS Elastic Beanstalk. See `.github/workflows/deploy.yml` for details.

