package me.huynhducphu.ping_me.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class SkipPathBearerTokenResolverTest {

    private final SkipPathBearerTokenResolver resolver = new SkipPathBearerTokenResolver();

    @Test
    void shouldSkipTokenResolutionForExactSkipPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/logout");
        request.addHeader("Authorization", "Bearer token-value");

        String token = resolver.resolve(request);

        assertThat(token).isNull();
    }

    @Test
    void shouldSkipTokenResolutionForHealthSubPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health/readiness");
        request.addHeader("Authorization", "Bearer token-value");

        String token = resolver.resolve(request);

        assertThat(token).isNull();
    }

    @Test
    void shouldResolveTokenForNonSkipPathContainingSkipWord() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/auth/logout-history");
        request.addHeader("Authorization", "Bearer token-value");

        String token = resolver.resolve(request);

        assertThat(token).isEqualTo("token-value");
    }
}
