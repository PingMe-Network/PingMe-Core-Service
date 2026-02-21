package org.ping_me.model.authorization;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.ping_me.model.common.BaseEntity;

import java.util.Set;

/**
 * Admin 10/25/2025
 *
 **/
@Entity
@Table(name = "permissions")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String name;

    String description;

    @ManyToMany(mappedBy = "permissions")
    @ToString.Exclude
    Set<Role> roles;
}
