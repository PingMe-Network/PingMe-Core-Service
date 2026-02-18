package me.huynhducphu.ping_me.dto.ws.chat.common;

/**
 * Admin 8/29/2025
 *
 **/
public enum ChatEventType {
    // Message
    MESSAGE_CREATED,
    MESSAGE_RECALLED,
    MESSAGE_TYPING,
    READ_STATE_CHANGED,


    // Room
    ROOM_CREATED,
    ROOM_UPDATED,
    ROOM_DELETED,

    // Group Membership
    MEMBER_ADDED,
    MEMBER_REMOVED,
    MEMBER_ROLE_CHANGED,
}
