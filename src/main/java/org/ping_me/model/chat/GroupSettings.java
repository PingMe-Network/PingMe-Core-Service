package org.ping_me.model.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.ping_me.model.common.BaseEntity;

/**
 * Group-level behavior toggles for room management screen.
 */
@Entity
@Table(name = "group_settings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GroupSettings extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "allow_member_edit_group_profile", nullable = false)
    private Boolean allowMemberEditGroupProfile = false;

    @Column(name = "allow_member_pin_message", nullable = false)
    private Boolean allowMemberPinMessage = false;

    @Column(name = "allow_member_create_note", nullable = false)
    @ColumnDefault("false")
    private Boolean allowMemberCreateNote = false;

    @Column(name = "allow_member_create_poll", nullable = false)
    private Boolean allowMemberCreatePoll = false;

    @Column(name = "allow_member_send_message", nullable = false)
    private Boolean allowMemberSendMessage = true;

    @Column(name = "join_approval_enabled", nullable = false)
    private Boolean joinApprovalEnabled = false;

    @Column(name = "highlight_admin_message_only", nullable = false)
    private Boolean highlightAdminMessageOnly = false;

    @Column(name = "allow_new_member_read_recent", nullable = false)
    private Boolean allowNewMemberReadRecent = true;

    @Column(name = "join_link_enabled", nullable = false)
    private Boolean joinLinkEnabled = true;

    @Column(name = "join_link_token", length = 128)
    private String joinLinkToken;
}
