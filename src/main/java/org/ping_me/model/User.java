package org.ping_me.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.ping_me.model.authorization.Role;
import org.ping_me.model.common.BaseEntity;
import org.ping_me.model.constant.AccountStatus;
import org.ping_me.model.constant.AuthProvider;
import org.ping_me.model.constant.Gender;
import org.ping_me.model.constant.UserStatus;

import java.time.LocalDate;

/**
 * Admin 8/3/2025
 **/
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * =====================================
     * Thông tin chung
     * =====================================
     */

    @Column(unique = true)
    String email;

    @Column(nullable = false)
    String name;

    @Enumerated(EnumType.STRING)
    Gender gender;

    String address;

    LocalDate dob;

    @Column(name = "avatar_url")
    String avatarUrl;

    /**
     * =====================================
     * Trạng thái tài khoản
     * =====================================
     */

    // Trạng thái hiện diện: ONLINE / OFFLINE
    @Enumerated(EnumType.STRING)
    UserStatus status;

    // Trạng thái tài khoản: NON_ACTIVATED / ACTIVE / SUSPENDED / DEACTIVATED
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    AccountStatus accountStatus;

    /**
     * =====================================
     * Xác thực và ủy quyền
     * =====================================
     */

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    AuthProvider authProvider;

    @JsonIgnore
    String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    Role role;


}
