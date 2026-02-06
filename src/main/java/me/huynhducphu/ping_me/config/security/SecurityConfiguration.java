package me.huynhducphu.ping_me.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Admin 8/3/2025
 **/
@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String[] WHITELIST = {
            // Authentication
            "/auth/login",
            "/auth/mobile/login",
            "/auth/logout",
            "/auth/register",
            "/auth/refresh",
            "/auth/admin/login",

            // Forget password
            "/auth/forget-password",
            "/mail-management/api/v1/mails/send-otp",
            "/mail-management/api/v1/mails/otp-verification",

            // OTP
            "/otp/send",
            "/otp/verify",

            // API DOCS
            "/swagger-ui/**",
            "/v3/api-docs/**",

            // WebSocket
            // Bỏ qua kiểm tra tại lớp BearerTokenFilter
            // Kiểm tra tại lớp HandShakeInterceptor
            "/ws/**",

            // Health check - Kiểm tra nhịp tim
            "/actuator/health",
            "/actuator/health/**",

    };

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            SkipPathBearerTokenResolver skipPathBearerTokenResolver
    ) throws Exception {
        httpSecurity
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITELIST).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .bearerTokenResolver(skipPathBearerTokenResolver)
                )
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return httpSecurity.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;

    }


}
