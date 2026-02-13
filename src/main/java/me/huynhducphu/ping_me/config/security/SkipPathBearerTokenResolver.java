package me.huynhducphu.ping_me.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Admin 8/9/2025
 **/
@Component
public class SkipPathBearerTokenResolver implements BearerTokenResolver {

    private final BearerTokenResolver delegate = new DefaultBearerTokenResolver();

    private final List<RequestMatcher> skipPathMatchers = List.of(
            new AntPathRequestMatcher("/auth/logout"),
            new AntPathRequestMatcher("/auth/register"),
            new AntPathRequestMatcher("/actuator/health"),
            new AntPathRequestMatcher("/actuator/health/**")
    );

    @Override
    public String resolve(HttpServletRequest request) {
        for (RequestMatcher skipPathMatcher : skipPathMatchers) {
            if (skipPathMatcher.matches(request)) {
                return null;
            }
        }

        return delegate.resolve(request);
    }

}
