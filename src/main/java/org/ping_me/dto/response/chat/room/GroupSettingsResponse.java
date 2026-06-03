package org.ping_me.dto.response.chat.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GroupSettingsResponse {
    private Long roomId;
    private Boolean allowMemberEditGroupProfile;
    private Boolean allowMemberPinMessage;
    private Boolean allowMemberCreateNote;
    private Boolean allowMemberCreatePoll;
    private Boolean allowMemberSendMessage;
    private Boolean joinApprovalEnabled;
    private Boolean highlightAdminMessageOnly;
    private Boolean allowNewMemberReadRecent;
    private Boolean joinLinkEnabled;
    private String joinLink;
}
