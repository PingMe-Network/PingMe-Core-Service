package me.huynhducphu.ping_me.model;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.authorization.Role;
import me.huynhducphu.ping_me.model.common.BaseEntity;
import me.huynhducphu.ping_me.model.constant.AccountStatus;
import me.huynhducphu.ping_me.model.constant.AuthProvider;
import me.huynhducphu.ping_me.model.constant.Gender;
import me.huynhducphu.ping_me.model.constant.UserStatus;

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
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String password;

    private String address;

    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    AccountStatus accountStatus;

}
