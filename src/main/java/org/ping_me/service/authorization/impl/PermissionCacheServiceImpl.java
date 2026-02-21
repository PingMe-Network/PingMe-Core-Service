package org.ping_me.service.authorization.impl;

import lombok.RequiredArgsConstructor;
import org.ping_me.model.authorization.Permission;
import org.ping_me.repository.jpa.auth.PermissionRepository;
import org.ping_me.service.authorization.PermissionCacheService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin 10/25/2025
 *
 **/
@Service
@RequiredArgsConstructor
@Transactional
public class PermissionCacheServiceImpl implements PermissionCacheService {

    private final PermissionRepository permissionRepository;

    @Cacheable(cacheNames = "role_permissions", key = "#role.toUpperCase()")
    @Override
    public List<String> getPermissionsByRole(String role) {
        String r = normalize(role);
        if (r.isEmpty()) return List.of();

        List<Permission> permissions = permissionRepository.findAllByRoleName(r);
        return permissions.stream().map(Permission::getName).toList();
    }

    @CacheEvict(cacheNames = "role_permissions", key = "#role.toUpperCase()")
    @Override
    public void invalidateRole(String role) {
    }

    @CacheEvict(cacheNames = "role_permissions", allEntries = true)
    @Override
    public void invalidateAll() {
    }

    private String normalize(String role) {
        return role == null ? "" : role.trim().toUpperCase();
    }


}
