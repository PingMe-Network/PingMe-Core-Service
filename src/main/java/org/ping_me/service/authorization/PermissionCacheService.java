package org.ping_me.service.authorization;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * Admin 10/26/2025
 *
 **/
public interface PermissionCacheService {
    @Cacheable(cacheNames = "role_permissions", key = "#role.toUpperCase()")
    List<String> getPermissionsByRole(String role);

    @CacheEvict(cacheNames = "role_permissions", key = "#role.toUpperCase()")
    void invalidateRole(String role);

    @CacheEvict(cacheNames = "role_permissions", allEntries = true)
    void invalidateAll();
}
