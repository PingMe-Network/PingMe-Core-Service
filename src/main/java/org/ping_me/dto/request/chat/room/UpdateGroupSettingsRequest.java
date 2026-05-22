package org.ping_me.dto.request.chat.room;

import lombok.Data;

/**
 * Nullable fields so client can partially update group settings.
 */
@Data
public class UpdateGroupSettingsRequest {
    private Boolean allowMemberEditGroupProfile;
    private Boolean allowMemberPinMessage;
    private Boolean allowMemberCreatePoll;
    private Boolean allowMemberSendMessage;
    private Boolean joinApprovalEnabled;
    private Boolean highlightAdminMessageOnly;
    private Boolean allowNewMemberReadRecent;
    private Boolean joinLinkEnabled;
}
