package org.ping_me.repository.jpa.auth;

import org.ping_me.model.authorization.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Admin 10/25/2025
 *
 **/
public interface RoleRepository extends JpaRepository<Role, Long> {
}
